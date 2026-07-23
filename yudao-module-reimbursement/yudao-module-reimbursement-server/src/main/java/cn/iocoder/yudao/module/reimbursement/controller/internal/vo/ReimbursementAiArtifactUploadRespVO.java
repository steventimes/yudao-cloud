package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;

/**
 * AI 附件暂存结果。
 */

@Data
public class ReimbursementAiArtifactUploadRespVO {
    private String externalArtifactId;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long size;
    private String sha256;
}
