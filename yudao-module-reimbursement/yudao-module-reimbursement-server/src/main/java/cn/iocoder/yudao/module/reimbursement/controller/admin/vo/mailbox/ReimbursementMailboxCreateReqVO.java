package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReimbursementMailboxCreateReqVO {
    @NotBlank
    private String providerCode;
    @NotBlank
    private String email;
    private String username;
    @NotBlank
    private String authorizationCode;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
}
