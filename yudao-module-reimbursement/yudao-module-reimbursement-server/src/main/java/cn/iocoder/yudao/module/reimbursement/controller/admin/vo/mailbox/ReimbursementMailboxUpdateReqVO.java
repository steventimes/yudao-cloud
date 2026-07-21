package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 请求参数。
 */

@Data
public class ReimbursementMailboxUpdateReqVO {
    /** 编号。 */
    @NotNull
    private Long id;
    /** 字段 providerCode。 */
    @NotBlank
    private String providerCode;
    /** 字段 email。 */
    @NotBlank
    private String email;
    /** 邮箱用户名。 */
    private String username;
    /** 邮箱授权码。 */
    private String authorizationCode;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
}
