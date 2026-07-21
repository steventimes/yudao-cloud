package cn.iocoder.yudao.module.reimbursement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReimbursementMailboxStatusEnum {
    UNVERIFIED(0), VERIFIED(1);

    private final Integer status;
}
