package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Email;
import lombok.Data;

/**
 * 创建报销邮箱连接请求。
 */

@Data
public class ReimbursementMailboxCreateReqVO {
    /** 邮箱服务商编码。 */
    @NotBlank
    private String providerCode;
    /** 邮箱地址。 */
    @Email
    @NotBlank
    private String email;
    /** 邮箱用户名。 */
    private String username;
    /** 邮箱授权码。 */
    @NotBlank
    private String authorizationCode;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
}
