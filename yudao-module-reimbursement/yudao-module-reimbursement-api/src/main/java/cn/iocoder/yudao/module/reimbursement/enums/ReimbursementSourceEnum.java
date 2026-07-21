package cn.iocoder.yudao.module.reimbursement.enums;

/**
 * 报销单的数据来源。
 */
public enum ReimbursementSourceEnum {
    /** 用户手工创建。 */
    MANUAL,
    /** 由 AI 邮件导入流程创建。 */
    AI_EMAIL
}
