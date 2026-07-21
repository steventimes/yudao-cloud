package cn.iocoder.yudao.module.reimbursement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ReimbursementStatusEnum，枚举定义。
 */

@Getter
@AllArgsConstructor
public enum ReimbursementStatusEnum {
    DRAFT(0, "草稿"), AI_PROCESSING(10, "AI 处理中"), PENDING_CONFIRMATION(20, "AI 待确认"), AI_FAILED(30, "AI 处理失败"),
    SUBMITTED(40, "已提交审批");

    private final Integer status;
    private final String name;
}
