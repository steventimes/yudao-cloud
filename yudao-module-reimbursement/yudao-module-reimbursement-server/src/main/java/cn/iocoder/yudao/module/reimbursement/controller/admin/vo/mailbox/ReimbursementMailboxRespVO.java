package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报销邮箱连接响应，不包含邮箱授权码。
 */

@Data
public class ReimbursementMailboxRespVO {
    /** 编号。 */
    private Long id;
    private String providerCode;
    private String emailNormalized;
    /** 邮箱用户名。 */
    private String username;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
    /** 状态。 */
    private Integer status;
    private LocalDateTime verifiedAt;
    private String lastFailureMessage;
}
