package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;

@Data
public class ReimbursementMailAccessResolveRespVO {
    private String executionId;
    private String connectionId;
    private String tenantId;
    private String providerCode;
    private String imapHost;
    private Integer imapPort;
    private String username;
    private String authorizationCode;
    private String credentialVersion;
    private String tlsVerification;
    private String expiresTime;
}
