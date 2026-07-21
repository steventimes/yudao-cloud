package cn.iocoder.yudao.module.reimbursement.enums;

/**
 * AI 处理完成后的报销单提交模式。
 */
public enum ReimbursementAiSubmitModeEnum {
    /** 仅生成 AI 草稿，等待人工确认。 */
    DRAFT_ONLY,
    /** AI 填充完成后自动提交审批。 */
    AUTO_SUBMIT
}
