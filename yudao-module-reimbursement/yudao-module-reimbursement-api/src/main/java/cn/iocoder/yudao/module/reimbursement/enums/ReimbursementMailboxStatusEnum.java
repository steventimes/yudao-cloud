package cn.iocoder.yudao.module.reimbursement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报销邮箱绑定的验证状态。
 */

@Getter
@AllArgsConstructor
public enum ReimbursementMailboxStatusEnum {
    /** 尚未完成邮箱验证。 */
    UNVERIFIED(0),
    /** 已完成邮箱验证。 */
    VERIFIED(1);

    /** 持久化使用的状态值。 */
    private final Integer status;
}
