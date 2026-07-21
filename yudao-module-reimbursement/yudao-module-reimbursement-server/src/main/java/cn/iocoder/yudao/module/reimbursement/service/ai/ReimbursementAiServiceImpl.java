package cn.iocoder.yudao.module.reimbursement.service.ai;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimSubmitRespVO;
import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementAttachmentDO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementItemDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementAttachmentMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementClaimMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementItemMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementAiSubmitModeEnum;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementSourceEnum;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.*;

/**
 * 报销 AI 回调 Service 实现类
 *
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class ReimbursementAiServiceImpl implements ReimbursementAiService {

    private static final Pattern SHA256_PATTERN = Pattern.compile("^[0-9a-f]{64}$");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf", "image/png", "image/jpeg", "image/jpg", "image/webp");
    private static final Set<String> ALLOWED_DOCUMENT_TYPES = Set.of("INVOICE", "RECEIPT", "OTHER");

    private final FileApi fileApi;
    private final ReimbursementProperties reimbursementProperties;
    private final ReimbursementClaimMapper claimMapper;
    private final ReimbursementItemMapper itemMapper;
    private final ReimbursementAttachmentMapper attachmentMapper;
    private final ReimbursementClaimService claimService;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional
    public ReimbursementAiArtifactUploadRespVO uploadAiArtifact(Long tenantId, Long reimbursementId,
            String externalArtifactId, String sha256,
            String documentType, MultipartFile file) {
        validateArtifactRequest(externalArtifactId, sha256, documentType, file);
        ReimbursementClaimDO claim = requireAiEmailClaim(reimbursementId);
        if (!Set.of(10, 20, 30).contains(claim.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        byte[] content = readFileBytes(file);
        if (!sha256.equals(sha256Hex(content))) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }

        ReimbursementAttachmentDO existingAttachment = attachmentMapper.selectByExternalArtifactId(
                reimbursementId, externalArtifactId);
        if (existingAttachment != null) {
            if (!sha256.equals(existingAttachment.getSha256())) {
                throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_DUPLICATE);
            }
            return buildArtifactUploadRespVO(existingAttachment);
        }
        if (attachmentMapper.selectCountByReimbursementId(reimbursementId) >= 20) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }

        String originalFilename = StrUtil.maxLength(file.getOriginalFilename(), 255);
        String fileUrl = fileApi.createFile(content, originalFilename,
                "reimbursement/" + reimbursementId, file.getContentType());
        ReimbursementAttachmentDO attachment = new ReimbursementAttachmentDO();
        attachment.setReimbursementId(reimbursementId);
        attachment.setExternalArtifactId(externalArtifactId);
        attachment.setFileUrl(fileUrl);
        attachment.setFileName(originalFilename);
        attachment.setMimeType(file.getContentType());
        attachment.setSize(file.getSize());
        attachment.setSha256(sha256);
        attachment.setDocumentType(documentType);
        attachmentMapper.insert(attachment);
        return buildArtifactUploadRespVO(attachment);
    }

    @Override
    public ReimbursementAiFillRespVO applyAiFill(Long tenantId, ReimbursementAiFillReqVO reqVO) {
        ReimbursementClaimDO claim = requireAiEmailClaim(reqVO.getReimbursementId());
        if (Objects.equals(claim.getStatus(), ReimbursementStatusEnum.SUBMITTED.getStatus())
                || Objects.equals(claim.getStatus(), ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus())) {
            return buildAiFillRespVO(claim, false, false, "AI 结果已存在");
        }
        if (!Set.of(10, 30).contains(claim.getStatus())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
        }
        validateAiFill(reqVO);

        transactionTemplate.executeWithoutResult(status -> {
            itemMapper.deleteByReimbursementId(claim.getId());
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ReimbursementAiFillReqVO.Item itemReqVO : reqVO.getItems()) {
                ReimbursementItemDO item = insertAiItem(claim.getId(), itemReqVO);
                totalAmount = totalAmount.add(item.getAmount());
                linkAttachments(claim.getId(), item.getId(), itemReqVO.getAttachmentExternalArtifactIds());
            }
            claim.setReason(StrUtil.blankToDefault(reqVO.getReason(), ""));
            claim.setCurrency("CNY");
            claim.setTotalAmount(totalAmount);
            claim.setAiConfidence(reqVO.getAiConfidence());
            claim.setAiFailureMessage(null);
            claim.setStatus(ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus());
            claimMapper.updateById(claim);
        });

        if (reimbursementProperties.getAi().getSubmitMode() == ReimbursementAiSubmitModeEnum.DRAFT_ONLY) {
            return buildAiFillRespVO(claim, false, false, "AI 草稿已生成，等待人工确认");
        }
        try {
            ReimbursementClaimSubmitRespVO submitRespVO = claimService.autoSubmitClaim(
                    tenantId, claim.getUserId(), claim.getId());
            claim.setStatus(submitRespVO.getStatus());
            claim.setProcessInstanceId(submitRespVO.getProcessInstanceId());
            return buildAiFillRespVO(claim, true, true, "AI 草稿已自动提交");
        } catch (Exception ex) {
            return buildAiFillRespVO(claim, true, false, "AI 草稿已生成，自动提交失败，请人工确认");
        }
    }

    private void validateArtifactRequest(String externalArtifactId, String sha256, String documentType, MultipartFile file) {
        if (StrUtil.isBlank(externalArtifactId) || externalArtifactId.length() > 128
                || !SHA256_PATTERN.matcher(sha256).matches() || file == null || file.isEmpty()
                || file.getSize() > 10L * 1024 * 1024 || !ALLOWED_MIME_TYPES.contains(file.getContentType())
                || !ALLOWED_DOCUMENT_TYPES.contains(documentType)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
    }

    private ReimbursementClaimDO requireAiEmailClaim(Long reimbursementId) {
        ReimbursementClaimDO claim = claimMapper.selectById(reimbursementId);
        if (claim == null) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_NOT_EXISTS);
        }
        if (!ReimbursementSourceEnum.AI_EMAIL.name().equals(claim.getSource())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_FILL_INVALID);
        }
        return claim;
    }

    private void validateAiFill(ReimbursementAiFillReqVO reqVO) {
        if (reqVO.getTenantId() == null || reqVO.getTenantId() <= 0
                || reqVO.getReimbursementId() == null || reqVO.getReimbursementId() <= 0
                || !"CNY".equals(reqVO.getCurrency()) || reqVO.getItems() == null || reqVO.getItems().isEmpty()
                || reqVO.getAiConfidence() == null || reqVO.getAiConfidence().compareTo(BigDecimal.ZERO) < 0
                || reqVO.getAiConfidence().compareTo(BigDecimal.ONE) > 0) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_FILL_INVALID);
        }
        Set<String> clientItemIds = new HashSet<>();
        Set<String> linkedArtifactIds = new HashSet<>();
        for (ReimbursementAiFillReqVO.Item itemReqVO : reqVO.getItems()) {
            if (itemReqVO.getExpenseDate() == null || itemReqVO.getAmount() == null
                    || itemReqVO.getAmount().signum() <= 0 || itemReqVO.getTaxAmount() != null
                    && (itemReqVO.getTaxAmount().signum() < 0
                    || itemReqVO.getTaxAmount().compareTo(itemReqVO.getAmount()) > 0)
                    || itemReqVO.getAiConfidence() == null
                    || itemReqVO.getAiConfidence().compareTo(BigDecimal.ZERO) < 0
                    || itemReqVO.getAiConfidence().compareTo(BigDecimal.ONE) > 0) {
                throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_FILL_INVALID);
            }
            try {
                cn.iocoder.yudao.module.reimbursement.enums.ReimbursementExpenseTypeEnum.valueOf(itemReqVO.getExpenseType());
            } catch (Exception ex) {
                throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_FILL_INVALID);
            }
            String clientItemId = StrUtil.blankToDefault(itemReqVO.getClientItemId(), UUID.randomUUID().toString());
            itemReqVO.setClientItemId(clientItemId);
            if (!clientItemIds.add(clientItemId)) {
                throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_FILL_INVALID);
            }
            for (String externalArtifactId : Optional.ofNullable(itemReqVO.getAttachmentExternalArtifactIds())
                    .orElse(Collections.emptyList())) {
                if (!linkedArtifactIds.add(externalArtifactId)
                        || attachmentMapper.selectByExternalArtifactId(reqVO.getReimbursementId(), externalArtifactId) == null) {
                    throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
                }
            }
        }
    }

    private ReimbursementItemDO insertAiItem(Long reimbursementId, ReimbursementAiFillReqVO.Item itemReqVO) {
        ReimbursementItemDO item = new ReimbursementItemDO();
        item.setReimbursementId(reimbursementId);
        item.setClientItemId(StrUtil.blankToDefault(itemReqVO.getClientItemId(), UUID.randomUUID().toString()));
        item.setExpenseDate(itemReqVO.getExpenseDate());
        item.setExpenseType(itemReqVO.getExpenseType());
        item.setMerchantName(itemReqVO.getMerchantName());
        item.setAmount(itemReqVO.getAmount());
        item.setTaxAmount(itemReqVO.getTaxAmount());
        item.setInvoiceNumber(itemReqVO.getInvoiceNumber());
        item.setRemark(itemReqVO.getRemark());
        item.setAiConfidence(itemReqVO.getAiConfidence());
        item.setManuallyModified(false);
        itemMapper.insert(item);
        return item;
    }

    private void linkAttachments(Long reimbursementId, Long itemId, List<String> externalArtifactIds) {
        for (String externalArtifactId : Optional.ofNullable(externalArtifactIds).orElse(Collections.emptyList())) {
            attachmentMapper.updateItemId(reimbursementId, externalArtifactId, itemId);
        }
    }

    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
    }

    private String sha256Hex(byte[] content) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(content);
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
    }

    private ReimbursementAiArtifactUploadRespVO buildArtifactUploadRespVO(ReimbursementAttachmentDO attachment) {
        ReimbursementAiArtifactUploadRespVO respVO = new ReimbursementAiArtifactUploadRespVO();
        respVO.setExternalArtifactId(attachment.getExternalArtifactId());
        respVO.setFileUrl(attachment.getFileUrl());
        respVO.setFileName(attachment.getFileName());
        respVO.setMimeType(attachment.getMimeType());
        respVO.setSize(attachment.getSize());
        respVO.setSha256(attachment.getSha256());
        return respVO;
    }

    private ReimbursementAiFillRespVO buildAiFillRespVO(ReimbursementClaimDO claim, boolean autoSubmitAttempted,
            boolean autoSubmitSucceeded, String message) {
        ReimbursementAiFillRespVO respVO = new ReimbursementAiFillRespVO();
        respVO.setReimbursementId(claim.getId());
        respVO.setStatus(claim.getStatus());
        respVO.setSubmitMode(reimbursementProperties.getAi().getSubmitMode().name());
        respVO.setAutoSubmitAttempted(autoSubmitAttempted);
        respVO.setAutoSubmitSucceeded(autoSubmitSucceeded);
        respVO.setProcessInstanceId(claim.getProcessInstanceId());
        respVO.setMessage(message);
        return respVO;
    }

}
