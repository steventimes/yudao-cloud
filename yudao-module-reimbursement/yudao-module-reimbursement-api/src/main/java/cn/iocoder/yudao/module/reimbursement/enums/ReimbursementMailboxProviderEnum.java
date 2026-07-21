package cn.iocoder.yudao.module.reimbursement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ReimbursementMailboxProviderEnum，枚举定义。
 */

@Getter
@AllArgsConstructor
public enum ReimbursementMailboxProviderEnum {
    QQ_MAIL("imap.qq.com", 993, "strict"), CUSTOM_IMAPS(null, null, null);

    private final String host;
    private final Integer port;
    private final String tlsVerification;
}
