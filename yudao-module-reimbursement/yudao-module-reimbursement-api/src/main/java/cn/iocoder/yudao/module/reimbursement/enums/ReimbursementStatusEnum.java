package cn.iocoder.yudao.module.reimbursement.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 报销单在业务流程中的状态。
 */

@Getter
@AllArgsConstructor
public enum ReimbursementStatusEnum {
    /** 普通草稿。 */
    DRAFT(0, "草稿"),
    /** AI 正在处理。 */
    AI_PROCESSING(10, "AI 处理中"),
    /** AI 处理完成，等待人工确认。 */
    PENDING_CONFIRMATION(20, "AI 待确认"),
    /** AI 处理失败。 */
    AI_FAILED(30, "AI 处理失败"),
    /** 已提交审批流程。 */
    SUBMITTED(40, "已提交审批");

    /** 持久化使用的状态值。 */
    private final Integer status;
    /** 面向展示的状态名称。 */
    private final String name;
}
