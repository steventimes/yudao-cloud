package cn.iocoder.yudao.module.reimbursement.service.ai;

import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Dify Workflow 与邮件插件调用的报销内部服务。
 */
public interface ReimbursementAiService {

    /** 校验内容哈希后暂存 Dify/邮件插件上传的报销附件。 */
    ReimbursementAiArtifactUploadRespVO uploadAiArtifact(Long tenantId, Long reimbursementId,
            String externalArtifactId, String sha256,
            String documentType, MultipartFile file);

    /** 校验并应用 AI 识别结果，按配置保留待确认草稿或尝试自动提交。 */
    ReimbursementAiFillRespVO applyAiFill(Long tenantId, ReimbursementAiFillReqVO reqVO);

}
