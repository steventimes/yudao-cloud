package cn.iocoder.yudao.module.reimbursement.service.ai;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
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

    /**
     * 上传报销附件。
     * 
     * @param tenantId           租户编号
     * @param reimbursementId    报销单编号
     * @param externalArtifactId 外部附件产物编号
     * @param sha256             文件 SHA-256 摘要
     * @param documentType       单据类型
     * @param file               上传的附件文件
     * @return 处理结果
     */
    @Override
    @Transactional
    public ReimbursementAiArtifactUploadRespVO uploadAiArtifact(Long tenantId, Long reimbursementId,
            String externalArtifactId, String sha256,
            String documentType, MultipartFile file) {
        validateArtifactRequest(externalArtifactId, sha256, documentType, file);
        ReimbursementClaimDO claim = requireAiEmailClaim(reimbursementId);
        if (!CollectionUtils.containsAny(claim.getStatus(), 10, 20, 30)) {
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

    /**
     * 应用 AI 识别结果。
     * 
     * @param tenantId 租户编号
     * @param reqVO    请求参数对象
     * @return 处理结果
     */

    @Override
    public ReimbursementAiFillRespVO applyAiFill(Long tenantId, ReimbursementAiFillReqVO reqVO) {
        ReimbursementClaimDO initialClaim = requireAiEmailClaim(reqVO.getReimbursementId());
        if (Objects.equals(initialClaim.getStatus(), ReimbursementStatusEnum.SUBMITTED.getStatus())
                || Objects.equals(initialClaim.getStatus(), ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus())) {
            return buildAiFillRespVO(initialClaim, false, false, "AI 结果已存在");
        }
        validateAiFill(reqVO);

        // 1. 重新读取并校验当前 Claim，再在独立事务中保存 AI 明细和附件关联。
        ReimbursementClaimDO claim = transactionTemplate.execute(status -> {
            ReimbursementClaimDO current = requireAiEmailClaim(reqVO.getReimbursementId());
            if (!CollectionUtils.containsAny(current.getStatus(), 10, 30)) {
                throw ServiceExceptionUtil.exception(REIMBURSEMENT_CLAIM_STATUS_INVALID);
            }
            attachmentMapper.clearItemIdByReimbursementId(current.getId(), tenantId);
            itemMapper.deletePermanentlyByReimbursementId(current.getId(), tenantId);
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (ReimbursementAiFillReqVO.Item itemReqVO : reqVO.getItems()) {
                ReimbursementItemDO item = insertAiItem(current.getId(), itemReqVO);
                totalAmount = totalAmount.add(item.getAmount());
                linkAttachments(current.getId(), item.getId(), itemReqVO.getAttachmentExternalArtifactIds());
            }
            current.setReason(StrUtil.blankToDefault(reqVO.getReason(), ""));
            current.setCurrency("CNY");
            current.setTotalAmount(totalAmount);
            current.setAiConfidence(reqVO.getAiConfidence());
            current.setAiFailureMessage(null);
            current.setStatus(ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus());
            claimMapper.updateById(current);
            return current;
        });

        // 2. 本地事务提交后再调用 BPM，远程调用失败不会回滚已保存的 AI 草稿。
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

    /**
     * 校验ArtifactRequest参数。
     * 
     * @param externalArtifactId 外部附件产物编号
     * @param sha256             文件 SHA-256 摘要
     * @param documentType       单据类型
     * @param file               上传的附件文件
     */
    private void validateArtifactRequest(String externalArtifactId, String sha256, String documentType,
            MultipartFile file) {
        if (StrUtil.isBlank(externalArtifactId) || externalArtifactId.length() > 128
                || !SHA256_PATTERN.matcher(sha256).matches() || file == null || file.isEmpty()
                || file.getSize() > 10L * 1024 * 1024 || !ALLOWED_MIME_TYPES.contains(file.getContentType())
                || !ALLOWED_DOCUMENT_TYPES.contains(documentType)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
    }

    /**
     * 获取并校验AiEmailClaim数据。
     * 
     * @param reimbursementId 报销单编号
     */
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

    /**
     * 校验AiFill参数。
     * 
     * @param reqVO 请求参数对象
     */
    private void validateAiFill(ReimbursementAiFillReqVO reqVO) {
        if (reqVO.getTenantId() == null || reqVO.getTenantId() <= 0
                || reqVO.getReimbursementId() == null || reqVO.getReimbursementId() <= 0
                || !"CNY".equals(reqVO.getCurrency()) || reqVO.getItems() == null || reqVO.getItems().isEmpty()
                || reqVO.getAiConfidence() == null || reqVO.getAiConfidence().compareTo(BigDecimal.ZERO) < 0
                || reqVO.getAiConfidence().compareTo(BigDecimal.ONE) > 0) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_FILL_INVALID);
        }
        // 识别结果同时校验明细编号和附件编号，避免一次回调重复覆盖业务数据。
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
                cn.iocoder.yudao.module.reimbursement.enums.ReimbursementExpenseTypeEnum
                        .valueOf(itemReqVO.getExpenseType());
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
                        || attachmentMapper.selectByExternalArtifactId(reqVO.getReimbursementId(),
                                externalArtifactId) == null) {
                    throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
                }
            }
        }
    }

    /**
     * 创建并保存AiItem数据。
     * 
     * @param reimbursementId 报销单编号
     * @param itemReqVO       报销明细请求对象
     */
    private ReimbursementItemDO insertAiItem(Long reimbursementId, ReimbursementAiFillReqVO.Item itemReqVO) {
        ReimbursementItemDO item = BeanUtils.toBean(itemReqVO, ReimbursementItemDO.class);
        item.setReimbursementId(reimbursementId);
        item.setClientItemId(StrUtil.blankToDefault(itemReqVO.getClientItemId(), UUID.randomUUID().toString()));
        item.setManuallyModified(false);
        itemMapper.insert(item);
        return item;
    }

    /**
     * 关联Attachments数据。
     * 
     * @param reimbursementId     报销单编号
     * @param itemId              明细编号
     * @param externalArtifactIds 外部附件产物编号列表
     */
    private void linkAttachments(Long reimbursementId, Long itemId, List<String> externalArtifactIds) {
        for (String externalArtifactId : Optional.ofNullable(externalArtifactIds).orElse(Collections.emptyList())) {
            attachmentMapper.updateItemId(reimbursementId, externalArtifactId, itemId);
        }
    }

    /**
     * 处理readFileBytes逻辑。
     * 
     * @param file 上传的附件文件
     */
    private byte[] readFileBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_AI_ATTACHMENT_INVALID);
        }
    }

    /**
     * 处理sha256Hex逻辑。
     * 
     * @param content 方法调用所需的content数据
     */
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

    /**
     * 构建ArtifactUploadRespVO结果。
     * 
     * @param attachment 附件数据
     */
    private ReimbursementAiArtifactUploadRespVO buildArtifactUploadRespVO(ReimbursementAttachmentDO attachment) {
        return BeanUtils.toBean(attachment, ReimbursementAiArtifactUploadRespVO.class);
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
