package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReimbursementMailboxRespVO {
    private Long id;
    private String providerCode;
    private String emailNormalized;
    private String username;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
    private Integer status;
    private LocalDateTime verifiedAt;
    private String lastFailureMessage;
}
