package cn.iocoder.yudao.module.reimbursement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报销邮箱支持的服务商类型及连接配置。
 */

@Getter
@AllArgsConstructor
public enum ReimbursementMailboxProviderEnum {
    /** QQ 邮箱，使用固定 IMAPS 配置。 */
    QQ_MAIL("imap.qq.com", 993, "strict"),
    /** 自定义 IMAPS 邮箱，连接参数由用户提供。 */
    CUSTOM_IMAPS(null, null, null);

    /** IMAP 服务器地址。 */
    private final String host;
    /** IMAP 服务器端口。 */
    private final Integer port;
    /** TLS 校验模式。 */
    private final String tlsVerification;
}
