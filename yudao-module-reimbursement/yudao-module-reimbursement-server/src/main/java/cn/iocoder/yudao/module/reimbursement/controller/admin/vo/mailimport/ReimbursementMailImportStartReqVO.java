package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

/**
 * 启动邮件票据导入请求。
 */

@Data
public class ReimbursementMailImportStartReqVO {
    /** 邮箱连接编号。 */
    @NotNull(message = "请选择邮箱连接")
    private Long mailboxConnectionId;
    /** 邮箱文件夹。 */
    private String folder = "INBOX";
    /** 回溯天数。 */
    private Integer lookbackDays;
    /** 起始日期。 */
    private LocalDate fromDate;
    /** 结束日期。 */
    private LocalDate toDate;
    /** 是否仅处理未读邮件。 */
    private Boolean unreadOnly = false;
    /** 主题关键词。 */
    private String subjectKeywords = "发票,票据,报销,invoice,receipt";
    /** 发件人过滤关键字。 */
    private String senderContains;
    /** 最大处理邮件数。 */
    @Min(value = 1, message = "最大处理邮件数不能小于 1")
    @Max(value = 50, message = "最大处理邮件数不能大于 50")
    private Integer maxMessages = 20;
}
