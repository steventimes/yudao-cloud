package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
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
 * 报销单服务实现。
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
    private final SecurityFrameworkService securityFrameworkService;
    private final FileApi fileApi;

    private static final String QUERY_ALL_PERMISSION = "reimbursement:claim:query-all";

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
    public void deleteClaim(Long userId, Long id) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, id);
        if (!CollectionUtils.containsAny(claim.getStatus(), ReimbursementStatusEnum.DRAFT.getStatus(),
                ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus(),
                ReimbursementStatusEnum.AI_FAILED.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_DELETE_STATUS_INVALID);
        }
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        attachmentMapper.clearItemIdByReimbursementId(id, tenantId);
        attachmentMapper.deleteByReimbursementId(id, tenantId);
        itemMapper.deletePermanentlyByReimbursementId(id, tenantId);
        claimMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void updateClaim(Long userId, ReimbursementClaimUpdateReqVO updateReqVO) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, updateReqVO.getId());
        if (!CollectionUtils.containsAny(claim.getStatus(), ReimbursementStatusEnum.DRAFT.getStatus(),
                ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus(),
                ReimbursementStatusEnum.AI_FAILED.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        validateCurrency(updateReqVO.getCurrency());
        validateItems(updateReqVO.getItems());

        claim.setReason(updateReqVO.getReason());
        claim.setCurrency(SUPPORTED_CURRENCY);
        claim.setTotalAmount(calculateTotalAmount(updateReqVO.getItems()));
        claim.setStatus(ReimbursementStatusEnum.DRAFT.getStatus());
        claimMapper.updateById(claim);

        Long tenantId = TenantContextHolder.getRequiredTenantId();
        attachmentMapper.clearItemIdByReimbursementId(claim.getId(), tenantId);
        itemMapper.deletePermanentlyByReimbursementId(claim.getId(), tenantId);
        saveItems(claim.getId(), updateReqVO.getItems(), true);
    }

    @Override
    @Transactional
    public void confirmClaim(Long userId, ReimbursementClaimConfirmReqVO confirmReqVO) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, confirmReqVO.getId());
        if (!Objects.equals(claim.getStatus(), ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }

        updateClaim(userId, BeanUtils.toBean(confirmReqVO, ReimbursementClaimUpdateReqVO.class));
    }

    @Override
    @Transactional
    public ReimbursementClaimSubmitRespVO submitClaim(Long userId, ReimbursementClaimSubmitReqVO submitReqVO) {
        ReimbursementClaimDO claim = requireOwnedClaim(userId, submitReqVO.getId());
        if (StrUtil.isNotBlank(claim.getProcessInstanceId())) {
            return buildSubmitRespVO(claim);
        }
        if (!CollectionUtils.containsAny(claim.getStatus(), ReimbursementStatusEnum.DRAFT.getStatus(),
                ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        return submitInternal(userId, claim, submitReqVO.getStartUserSelectAssignees());
    }


    @Override
    public ReimbursementClaimRespVO getClaim(Long userId, Long id) {
        return buildClaimRespVO(requireAccessibleClaim(userId, id));
    }


    @Override
    public PageResult<ReimbursementClaimDO> getClaimPage(Long userId, ReimbursementClaimPageReqVO pageReqVO) {
        return claimMapper.selectPage(userId, hasQueryAllPermission(), pageReqVO);
    }


    @Override
    public String getAttachmentAccessUrl(Long userId, Long reimbursementId, Long attachmentId) {
        requireAccessibleClaim(userId, reimbursementId);
        ReimbursementAttachmentDO attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null || !Objects.equals(attachment.getReimbursementId(), reimbursementId)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
        try {
            return fileApi.presignGetUrl(attachment.getFileUrl(), 300).getCheckedData();
        } catch (UnsupportedOperationException ignored) {
            // local、ftp、db 等文件客户端通过原始 URL 直接读取，不支持也不需要预签名。
            return attachment.getFileUrl();
        }
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

        // 组装 BPM 所需业务变量，并使用报销单 ID 作为流程业务键，保证流程可追溯。
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

    /**
     * 构造 BPM 流程变量。
     * 
     * @param userId 用户编号
     * @param claim  报销单数据
     */
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

    /**
     * 构造审批提交响应。
     * 
     * @param claim 报销单数据
     */
    private ReimbursementClaimSubmitRespVO buildSubmitRespVO(ReimbursementClaimDO claim) {
        return BeanUtils.toBean(claim, ReimbursementClaimSubmitRespVO.class)
                .setReimbursementId(claim.getId());
    }

    /**
     * 查询当前用户拥有的报销单。
     * 
     * @param userId          用户编号
     * @param reimbursementId 报销单编号
     */
    private ReimbursementClaimDO requireOwnedClaim(Long userId, Long reimbursementId) {
        ReimbursementClaimDO claim = claimMapper.selectOwnedById(reimbursementId, userId);
        if (claim == null) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_NOT_EXISTS);
        }
        return claim;
    }

    /**
     * 按 query-all 权限查询当前用户可访问的报销单。
     * 
     * @param userId          用户编号
     * @param reimbursementId 报销单编号
     */
    private ReimbursementClaimDO requireAccessibleClaim(Long userId, Long reimbursementId) {
        // query-all 只扩大查询范围，详情和附件仍需经过同一套可访问性校验。
        ReimbursementClaimDO claim = claimMapper.selectByIdForUser(reimbursementId, userId, hasQueryAllPermission());
        if (claim == null) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_NOT_EXISTS);
        }
        return claim;
    }

    /** 判断当前用户是否拥有查询全部报销单的权限。 */
    private boolean hasQueryAllPermission() {
        return securityFrameworkService.hasPermission(QUERY_ALL_PERMISSION);
    }

    /**
     * 校验币种；当前仅支持 CNY。
     * 
     * @param currency 币种
     */
    private void validateCurrency(String currency) {
        if (StrUtil.isNotBlank(currency) && !SUPPORTED_CURRENCY.equals(currency)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CURRENCY_NOT_SUPPORTED);
        }
    }

    /**
     * 校验报销明细非空且客户端明细编号唯一。
     * 
     * @param itemReqVOList 报销明细请求列表
     */
    private void validateItems(List<ReimbursementItemReqVO> itemReqVOList) {
        if (itemReqVOList == null || itemReqVOList.isEmpty()) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_EMPTY);
        }
        Set<String> clientItemIds = new HashSet<>();
        for (ReimbursementItemReqVO itemReqVO : itemReqVOList) {
            validateItem(itemReqVO, clientItemIds);
        }
    }

    /**
     * 校验费用类型、金额、税额和客户端明细编号。
     * 
     * @param itemReqVO     报销明细请求对象
     * @param clientItemIds 已使用的明细客户端编号集合
     */
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

    /**
     * 汇总报销明细金额。
     * 
     * @param itemReqVOList 报销明细请求列表
     */
    private BigDecimal calculateTotalAmount(List<ReimbursementItemReqVO> itemReqVOList) {
        return itemReqVOList.stream()
                .map(ReimbursementItemReqVO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 重建报销明细；人工保存时保留已有附件关联。
     * 
     * @param reimbursementId  报销单编号
     * @param itemReqVOList    报销明细请求列表
     * @param manuallyModified 是否为人工修改数据
     */
    private void saveItems(Long reimbursementId, List<ReimbursementItemReqVO> itemReqVOList, boolean manuallyModified) {
        for (ReimbursementItemReqVO itemReqVO : itemReqVOList) {
            ReimbursementItemDO item = BeanUtils.toBean(itemReqVO, ReimbursementItemDO.class);
            item.setReimbursementId(reimbursementId);
            item.setManuallyModified(manuallyModified);
            itemMapper.insert(item);
        }
    }

    /**
     * 组装报销详情及其明细和附件。
     * 
     * @param claim 报销单数据
     */
    private ReimbursementClaimRespVO buildClaimRespVO(ReimbursementClaimDO claim) {
        ReimbursementClaimRespVO claimRespVO = BeanUtils.toBean(claim, ReimbursementClaimRespVO.class);
        claimRespVO.setItems(itemMapper.selectListByReimbursementId(claim.getId()).stream()
                .map(this::buildItemRespVO).collect(Collectors.toList()));
        claimRespVO.setAttachments(attachmentMapper.selectListByReimbursementId(claim.getId()).stream()
                .map(this::buildAttachmentRespVO).collect(Collectors.toList()));
        return claimRespVO;
    }

    /**
     * 将报销明细转换为响应对象。
     * 
     * @param item 报销明细数据
     */
    private ReimbursementItemRespVO buildItemRespVO(ReimbursementItemDO item) {
        return BeanUtils.toBean(item, ReimbursementItemRespVO.class);
    }

    /**
     * 将报销附件转换为响应对象。
     * 
     * @param attachment 附件数据
     */
    private ReimbursementAttachmentRespVO buildAttachmentRespVO(ReimbursementAttachmentDO attachment) {
        return BeanUtils.toBean(attachment, ReimbursementAttachmentRespVO.class);
    }

}
