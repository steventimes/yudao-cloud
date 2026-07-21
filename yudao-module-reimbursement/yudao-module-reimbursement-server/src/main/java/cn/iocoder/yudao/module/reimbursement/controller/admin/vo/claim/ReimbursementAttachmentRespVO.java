package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * 响应结果。
 */

@Data
public class ReimbursementAttachmentRespVO {
    /** 编号。 */
    private Long id;
    /** 明细编号。 */
    private Long itemId;
    private String externalArtifactId;
    private String fileName;
    private String mimeType;
    private Long size;
    private String sha256;
    private String documentType;
}
