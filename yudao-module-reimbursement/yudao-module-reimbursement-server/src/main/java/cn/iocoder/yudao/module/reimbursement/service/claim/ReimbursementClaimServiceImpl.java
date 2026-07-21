package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import cn.iocoder.yudao.module.bpm.api.task.dto.BpmProcessInstanceCreateReqDTO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementAttachmentDO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementItemDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementAttachmentMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementClaimMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementItemMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementExpenseTypeEnum;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementSourceEnum;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.*;

/**
 * 报销申请 Service 实现类
 *
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class ReimbursementClaimServiceImpl implements ReimbursementClaimService {

    private static final String BPM_PROCESS_DEFINITION_KEY = "oa_reimbursement";
    private static final String SUPPORTED_CURRENCY = "CNY";

    private final ReimbursementClaimMapper claimMapper;
    private final ReimbursementItemMapper itemMapper;
    private final ReimbursementAttachmentMapper attachmentMapper;
    private final AdminUserApi adminUserApi;
    private final BpmProcessInstanceApi bpmProcessInstanceApi;

    @Override
    @Transactional
    public Long createClaim(Long userId, ReimbursementClaimCreateReqVO createReqVO) {
        validateCurrency(createReqVO.getCurrency());
        validateItems(createReqVO.getItems());

        AdminUserRespDTO applicant = adminUserApi.getUser(userId).getCheckedData();
        ReimbursementClaimDO claim = buildManualDraftClaim(userId, applicant, createReqVO);
        claimMapper.insert(claim);

        claim.setReimbursementNo("RB" + claim.getId());
        claimMapper.updateById(claim);
        saveItems(claim.getId(), createReqVO.getItems(), false);
        return claim.getId();
    }

    @Override
    @Transactional
    public void updateClaim(Long userId, ReimbursementClaimUpdateReqVO updateReqVO) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, updateReqVO.getId());
        if (!Set.of(0, 20, 30).contains(claim.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        validateCurrency(updateReqVO.getCurrency());
        validateItems(updateReqVO.getItems());

        claim.setReason(updateReqVO.getReason());
        claim.setCurrency(SUPPORTED_CURRENCY);
        claim.setTotalAmount(calculateTotalAmount(updateReqVO.getItems()));
        claim.setStatus(ReimbursementStatusEnum.DRAFT.getStatus());
        claimMapper.updateById(claim);

        itemMapper.deleteByReimbursementId(claim.getId());
        saveItems(claim.getId(), updateReqVO.getItems(), true);
    }

    @Override
    @Transactional
    public void confirmClaim(Long userId, ReimbursementClaimConfirmReqVO confirmReqVO) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, confirmReqVO.getId());
        if (!Objects.equals(claim.getStatus(), ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }

        ReimbursementClaimUpdateReqVO updateReqVO = new ReimbursementClaimUpdateReqVO();
        updateReqVO.setId(confirmReqVO.getId());
        updateReqVO.setReason(confirmReqVO.getReason());
        updateReqVO.setCurrency(confirmReqVO.getCurrency());
        updateReqVO.setItems(confirmReqVO.getItems());
        updateClaim(userId, updateReqVO);
    }

    @Override
    @Transactional
    public ReimbursementClaimSubmitRespVO submitClaim(Long userId, ReimbursementClaimSubmitReqVO submitReqVO) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, submitReqVO.getId());
        if (StrUtil.isNotBlank(claim.getProcessInstanceId())) {
            return buildSubmitRespVO(claim);
        }
        if (!Objects.equals(claim.getStatus(), ReimbursementStatusEnum.DRAFT.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        return submitInternal(userId, claim, submitReqVO.getStartUserSelectAssignees());
    }

    @Override
    public ReimbursementClaimRespVO getClaim(Long userId, Long id) {
        return buildClaimRespVO(requireOwnedClaim(userId, id));
    }

    @Override
    public PageResult<ReimbursementClaimDO> getClaimPage(Long userId, ReimbursementClaimPageReqVO pageReqVO) {
        return claimMapper.selectPage(userId, false, pageReqVO);
    }

    @Override
    public String getAttachmentAccessUrl(Long userId, Long reimbursementId, Long attachmentId) {
        requireOwnedClaim(userId, reimbursementId);
        ReimbursementAttachmentDO attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || !Objects.equals(attachment.getReimbursementId(), reimbursementId)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
        return attachment.getFileUrl();
    }

    @Override
    @Transactional
    public Long createAiProcessingClaim(Long userId, Long mailboxConnectionId) {
        AdminUserRespDTO applicant = adminUserApi.getUser(userId).getCheckedData();
        ReimbursementClaimDO claim = new ReimbursementClaimDO();
        claim.setUserId(userId);
        claim.setDeptId(applicant == null ? null : applicant.getDeptId());
        claim.setApplicantNameSnapshot(applicant == null ? "" : StrUtil.blankToDefault(applicant.getNickname(), ""));
        claim.setReason("");
        claim.setCurrency(SUPPORTED_CURRENCY);
        claim.setTotalAmount(BigDecimal.ZERO);
        claim.setStatus(ReimbursementStatusEnum.AI_PROCESSING.getStatus());
        claim.setSource(ReimbursementSourceEnum.AI_EMAIL.name());
        claim.setMailboxConnectionId(mailboxConnectionId);
        claimMapper.insert(claim);

        claim.setReimbursementNo("RB" + claim.getId());
        claimMapper.updateById(claim);
        return claim.getId();
    }

    @Override
    public void markAiFailedIfProcessing(Long tenantId, Long reimbursementId, String failureMessage) {
        ReimbursementClaimDO claim = claimMapper.selectById(reimbursementId);
        if (claim == null || !Objects.equals(claim.getStatus(), ReimbursementStatusEnum.AI_PROCESSING.getStatus())) {
            return;
        }
        claim.setStatus(ReimbursementStatusEnum.AI_FAILED.getStatus());
        claim.setAiFailureMessage(StrUtil.maxLength(failureMessage, 512));
        claimMapper.updateById(claim);
    }

    @Override
    @Transactional
    public ReimbursementClaimSubmitRespVO autoSubmitClaim(Long tenantId, Long ownerUserId, Long reimbursementId) {
        ReimbursementClaimDO claim = requireOwnedClaim(ownerUserId, reimbursementId);
        if (!Objects.equals(claim.getStatus(), ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        return submitInternal(ownerUserId, claim, null);
    }

    private ReimbursementClaimDO buildManualDraftClaim(Long userId, AdminUserRespDTO applicant,
            ReimbursementClaimCreateReqVO createReqVO) {
        ReimbursementClaimDO claim = new ReimbursementClaimDO();
        claim.setUserId(userId);
        claim.setDeptId(applicant == null ? null : applicant.getDeptId());
        claim.setApplicantNameSnapshot(applicant == null ? "" : StrUtil.blankToDefault(applicant.getNickname(), ""));
        claim.setReason(createReqVO.getReason());
        claim.setCurrency(SUPPORTED_CURRENCY);
        claim.setStatus(ReimbursementStatusEnum.DRAFT.getStatus());
        claim.setSource(ReimbursementSourceEnum.MANUAL.name());
        claim.setTotalAmount(calculateTotalAmount(createReqVO.getItems()));
        return claim;
    }

    private ReimbursementClaimSubmitRespVO submitInternal(Long userId, ReimbursementClaimDO claim,
            Map<String, List<Long>> startUserSelectAssignees) {
        if (StrUtil.isNotBlank(claim.getProcessInstanceId())) {
            return buildSubmitRespVO(claim);
        }

        Map<String, Object> processVariables = buildProcessVariables(userId, claim);
        BpmProcessInstanceCreateReqDTO createProcessReqDTO = new BpmProcessInstanceCreateReqDTO();
        createProcessReqDTO.setProcessDefinitionKey(BPM_PROCESS_DEFINITION_KEY);
        createProcessReqDTO.setBusinessKey(String.valueOf(claim.getId()));
        createProcessReqDTO.setVariables(processVariables);
        createProcessReqDTO.setStartUserSelectAssignees(startUserSelectAssignees);

        String processInstanceId = bpmProcessInstanceApi.createProcessInstance(userId, createProcessReqDTO)
                .getCheckedData();
        claim.setStatus(ReimbursementStatusEnum.SUBMITTED.getStatus());
        claim.setProcessInstanceId(processInstanceId);
        claim.setSubmitTime(LocalDateTime.now());
        claimMapper.updateById(claim);
        return buildSubmitRespVO(claim);
    }

    private Map<String, Object> buildProcessVariables(Long userId, ReimbursementClaimDO claim) {
        Map<String, Object> processVariables = new LinkedHashMap<>();
        processVariables.put("reimbursementId", claim.getId());
        processVariables.put("initiator", userId);
        processVariables.put("totalAmount", claim.getTotalAmount());
        processVariables.put("itemCount", itemMapper.selectListByReimbursementId(claim.getId()).size());
        processVariables.put("source", claim.getSource());
        processVariables.put("currency", claim.getCurrency());
        processVariables.put("deptId", claim.getDeptId());
        return processVariables;
    }

    private ReimbursementClaimSubmitRespVO buildSubmitRespVO(ReimbursementClaimDO claim) {
        ReimbursementClaimSubmitRespVO submitRespVO = new ReimbursementClaimSubmitRespVO();
        submitRespVO.setReimbursementId(claim.getId());
        submitRespVO.setStatus(claim.getStatus());
        submitRespVO.setProcessInstanceId(claim.getProcessInstanceId());
        return submitRespVO;
    }

    private ReimbursementClaimDO requireOwnedClaim(Long userId, Long reimbursementId) {
        ReimbursementClaimDO claim = claimMapper.selectOwnedById(reimbursementId, userId);
        if (claim == null) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_NOT_EXISTS);
        }
        return claim;
    }

    private void validateCurrency(String currency) {
        if (StrUtil.isNotBlank(currency) && !SUPPORTED_CURRENCY.equals(currency)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CURRENCY_NOT_SUPPORTED);
        }
    }

    private void validateItems(List<ReimbursementItemReqVO> itemReqVOList) {
        if (itemReqVOList == null || itemReqVOList.isEmpty()) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_EMPTY);
        }
        Set<String> clientItemIds = new HashSet<>();
        for (ReimbursementItemReqVO itemReqVO : itemReqVOList) {
            validateItem(itemReqVO, clientItemIds);
        }
    }

    private void validateItem(ReimbursementItemReqVO itemReqVO, Set<String> clientItemIds) {
        try {
            ReimbursementExpenseTypeEnum.valueOf(itemReqVO.getExpenseType());
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_INVALID);
        }
        if (itemReqVO.getAmount() == null || itemReqVO.getAmount().signum() <= 0) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_INVALID);
        }
        BigDecimal taxAmount = itemReqVO.getTaxAmount();
        if (taxAmount != null && (taxAmount.signum() < 0 || taxAmount.compareTo(itemReqVO.getAmount()) > 0)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_INVALID);
        }
        String clientItemId = StrUtil.blankToDefault(itemReqVO.getClientItemId(), UUID.randomUUID().toString());
        if (!clientItemIds.add(clientItemId)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_INVALID);
        }
        itemReqVO.setClientItemId(clientItemId);
    }

    private BigDecimal calculateTotalAmount(List<ReimbursementItemReqVO> itemReqVOList) {
        return itemReqVOList.stream()
                .map(ReimbursementItemReqVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void saveItems(Long reimbursementId, List<ReimbursementItemReqVO> itemReqVOList, boolean manuallyModified) {
        for (ReimbursementItemReqVO itemReqVO : itemReqVOList) {
            ReimbursementItemDO item = new ReimbursementItemDO();
            item.setReimbursementId(reimbursementId);
            item.setClientItemId(itemReqVO.getClientItemId());
            item.setExpenseDate(itemReqVO.getExpenseDate());
            item.setExpenseType(itemReqVO.getExpenseType());
            item.setMerchantName(itemReqVO.getMerchantName());
            item.setAmount(itemReqVO.getAmount());
            item.setTaxAmount(itemReqVO.getTaxAmount());
            item.setInvoiceNumber(itemReqVO.getInvoiceNumber());
            item.setRemark(itemReqVO.getRemark());
            item.setManuallyModified(manuallyModified);
            itemMapper.insert(item);
        }
    }

    private ReimbursementClaimRespVO buildClaimRespVO(ReimbursementClaimDO claim) {
        ReimbursementClaimRespVO claimRespVO = new ReimbursementClaimRespVO();
        claimRespVO.setId(claim.getId());
        claimRespVO.setReimbursementNo(claim.getReimbursementNo());
        claimRespVO.setUserId(claim.getUserId());
        claimRespVO.setDeptId(claim.getDeptId());
        claimRespVO.setApplicantNameSnapshot(claim.getApplicantNameSnapshot());
        claimRespVO.setReason(claim.getReason());
        claimRespVO.setTotalAmount(claim.getTotalAmount());
        claimRespVO.setCurrency(claim.getCurrency());
        claimRespVO.setStatus(claim.getStatus());
        claimRespVO.setProcessInstanceId(claim.getProcessInstanceId());
        claimRespVO.setSource(claim.getSource());
        claimRespVO.setMailboxConnectionId(claim.getMailboxConnectionId());
        claimRespVO.setAiConfidence(claim.getAiConfidence());
        claimRespVO.setAiFailureMessage(claim.getAiFailureMessage());
        claimRespVO.setSubmitTime(claim.getSubmitTime());
        claimRespVO.setCreateTime(claim.getCreateTime());
        claimRespVO.setItems(itemMapper.selectListByReimbursementId(claim.getId()).stream()
                .map(this::buildItemRespVO).collect(Collectors.toList()));
        claimRespVO.setAttachments(attachmentMapper.selectListByReimbursementId(claim.getId()).stream()
                .map(this::buildAttachmentRespVO).collect(Collectors.toList()));
        return claimRespVO;
    }

    private ReimbursementItemRespVO buildItemRespVO(ReimbursementItemDO item) {
        ReimbursementItemRespVO itemRespVO = new ReimbursementItemRespVO();
        itemRespVO.setId(item.getId());
        itemRespVO.setClientItemId(item.getClientItemId());
        itemRespVO.setExpenseDate(item.getExpenseDate());
        itemRespVO.setExpenseType(item.getExpenseType());
        itemRespVO.setMerchantName(item.getMerchantName());
        itemRespVO.setAmount(item.getAmount());
        itemRespVO.setTaxAmount(item.getTaxAmount());
        itemRespVO.setInvoiceNumber(item.getInvoiceNumber());
        itemRespVO.setRemark(item.getRemark());
        itemRespVO.setAiConfidence(item.getAiConfidence());
        itemRespVO.setManuallyModified(item.getManuallyModified());
        return itemRespVO;
    }

    private ReimbursementAttachmentRespVO buildAttachmentRespVO(ReimbursementAttachmentDO attachment) {
        ReimbursementAttachmentRespVO attachmentRespVO = new ReimbursementAttachmentRespVO();
        attachmentRespVO.setId(attachment.getId());
        attachmentRespVO.setItemId(attachment.getItemId());
        attachmentRespVO.setExternalArtifactId(attachment.getExternalArtifactId());
        attachmentRespVO.setFileUrl(attachment.getFileUrl());
        attachmentRespVO.setFileName(attachment.getFileName());
        attachmentRespVO.setMimeType(attachment.getMimeType());
        attachmentRespVO.setSize(attachment.getSize());
        attachmentRespVO.setSha256(attachment.getSha256());
        attachmentRespVO.setDocumentType(attachment.getDocumentType());
        return attachmentRespVO;
    }

}
