package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementAttachmentRespVO {
    private Long id;
    private Long itemId;
    private String externalArtifactId;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long size;
    private String sha256;
    private String documentType;
}
