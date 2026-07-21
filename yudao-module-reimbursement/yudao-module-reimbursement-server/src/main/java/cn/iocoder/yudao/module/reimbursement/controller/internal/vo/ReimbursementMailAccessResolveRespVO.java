package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;

/**
 * 响应结果。
 */

@Data
public class ReimbursementMailAccessResolveRespVO {
    private String executionId;
    private String connectionId;
    /** 租户编号。 */
    private String tenantId;
    private String providerCode;
    private String imapHost;
    private Integer imapPort;
    /** 邮箱用户名。 */
    private String username;
    /** 邮箱授权码。 */
    private String authorizationCode;
    private String credentialVersion;
    private String tlsVerification;
    private String expiresTime;
}
