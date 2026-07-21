package cn.iocoder.yudao.module.reimbursement.service.ai;

import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 报销 AI 回调 Service
 *
 * @author Codex
 */
public interface ReimbursementAiService {

    ReimbursementAiArtifactUploadRespVO uploadAiArtifact(Long tenantId, Long reimbursementId,
            String externalArtifactId, String sha256,
            String documentType, MultipartFile file);

    ReimbursementAiFillRespVO applyAiFill(Long tenantId, ReimbursementAiFillReqVO reqVO);

}
