package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReimbursementMailImportStartReqVO {
    @NotNull
    private Long mailboxConnectionId;
    private String folder = "INBOX";
    private Integer lookbackDays = 30;
    private LocalDate fromDate;
    private LocalDate toDate;
    private Boolean unreadOnly = false;
    private String subjectKeywords = "发票,票据,报销,invoice,receipt";
    private String senderContains;
    @Min(1)
    @Max(50)
    private Integer maxMessages = 20;
}
