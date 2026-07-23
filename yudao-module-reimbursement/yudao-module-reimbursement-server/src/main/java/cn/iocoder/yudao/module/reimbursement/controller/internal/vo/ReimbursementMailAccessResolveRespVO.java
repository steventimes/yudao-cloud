package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;

/**
 * 内部邮箱连接解析结果，包含短期授权范围内的解密凭据。
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
