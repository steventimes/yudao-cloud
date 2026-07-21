package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementClaimRespVO {
    private Long id;
    private String reimbursementNo;
    private Long userId;
    private Long deptId;
    private String applicantNameSnapshot;
    private String reason;
    private BigDecimal totalAmount;
    private String currency;
    private Integer status;
    private String processInstanceId;
    private String source;
    private Long mailboxConnectionId;
    private BigDecimal aiConfidence;
    private String aiFailureMessage;
    private LocalDateTime submitTime;
    private LocalDateTime createTime;
    private List<ReimbursementItemRespVO> items;
    private List<ReimbursementAttachmentRespVO> attachments;
}
