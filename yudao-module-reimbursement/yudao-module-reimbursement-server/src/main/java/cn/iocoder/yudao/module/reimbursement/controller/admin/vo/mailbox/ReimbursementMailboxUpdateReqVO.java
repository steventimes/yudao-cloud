package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReimbursementMailboxUpdateReqVO {
    @NotNull
    private Long id;
    @NotBlank
    private String providerCode;
    @NotBlank
    private String email;
    private String username;
    private String authorizationCode;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
}
