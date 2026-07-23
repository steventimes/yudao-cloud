package cn.iocoder.yudao.module.reimbursement.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * 报销模块业务错误码常量。
 */
public interface ErrorCodeConstants {
    /** 报销单不存在。 */
    ErrorCode REIMBURSEMENT_CLAIM_NOT_EXISTS = new ErrorCode(1_070_000_000, "报销单不存在");
    /** 无权访问报销单。 */
    ErrorCode REIMBURSEMENT_CLAIM_NOT_OWNER = new ErrorCode(1_070_000_001, "无权访问该报销单");
    /** 报销单状态不允许操作。 */
    ErrorCode REIMBURSEMENT_CLAIM_STATUS_INVALID = new ErrorCode(1_070_000_002, "报销单状态不允许该操作");
    /** 报销单删除状态不允许。 */
    ErrorCode REIMBURSEMENT_CLAIM_DELETE_STATUS_INVALID = new ErrorCode(1_070_000_020, "仅草稿或 AI 失败报销单可删除");
    /** 报销明细为空。 */
    ErrorCode REIMBURSEMENT_ITEM_EMPTY = new ErrorCode(1_070_000_003, "报销明细不能为空");
    /** 报销明细不合法。 */
    ErrorCode REIMBURSEMENT_ITEM_INVALID = new ErrorCode(1_070_000_004, "报销明细不合法");
    /** 不支持的币种。 */
    ErrorCode REIMBURSEMENT_CURRENCY_NOT_SUPPORTED = new ErrorCode(1_070_000_005, "仅支持 CNY 币种");
    /** 邮箱绑定不存在。 */
    ErrorCode REIMBURSEMENT_MAILBOX_NOT_EXISTS = new ErrorCode(1_070_000_006, "邮箱绑定不存在");
    /** 无权访问邮箱绑定。 */
    ErrorCode REIMBURSEMENT_MAILBOX_NOT_OWNER = new ErrorCode(1_070_000_007, "无权访问该邮箱绑定");
    /** 邮箱绑定未验证。 */
    ErrorCode REIMBURSEMENT_MAILBOX_NOT_VERIFIED = new ErrorCode(1_070_000_008, "邮箱绑定未验证");
    /** 邮箱配置不合法。 */
    ErrorCode REIMBURSEMENT_MAILBOX_CONFIG_INVALID = new ErrorCode(1_070_000_009, "邮箱配置不合法");
    /** 邮箱授权码无效。 */
    ErrorCode REIMBURSEMENT_MAILBOX_CREDENTIAL_INVALID = new ErrorCode(1_070_000_010, "邮箱授权码无效");
    /** 自定义邮箱未启用。 */
    ErrorCode REIMBURSEMENT_MAILBOX_CUSTOM_DISABLED = new ErrorCode(1_070_000_011, "自定义 IMAPS 邮箱未启用");
    /** 邮箱访问令牌无效。 */
    ErrorCode REIMBURSEMENT_MAIL_ACCESS_TOKEN_INVALID = new ErrorCode(1_070_000_012, "邮箱访问 Token 无效");
    /** 邮箱访问操作被拒绝。 */
    ErrorCode REIMBURSEMENT_MAIL_ACCESS_OPERATION_DENIED = new ErrorCode(1_070_000_013, "邮箱访问操作不允许");
    /** Dify 未配置。 */
    ErrorCode REIMBURSEMENT_DIFY_NOT_CONFIGURED = new ErrorCode(1_070_000_014, "Dify 未配置");
    /** Dify 请求失败。 */
    ErrorCode REIMBURSEMENT_DIFY_REQUEST_FAILED = new ErrorCode(1_070_000_015, "Dify 请求失败");
    /** 内部服务令牌无效。 */
    ErrorCode REIMBURSEMENT_INTERNAL_TOKEN_INVALID = new ErrorCode(1_070_000_016, "内部服务 Token 无效");
    /** AI 填充数据不合法。 */
    ErrorCode REIMBURSEMENT_AI_FILL_INVALID = new ErrorCode(1_070_000_017, "AI 填充数据不合法");
    /** AI 附件不合法。 */
    ErrorCode REIMBURSEMENT_AI_ATTACHMENT_INVALID = new ErrorCode(1_070_000_018, "AI 附件不合法");
    /** AI 附件重复或冲突。 */
    ErrorCode REIMBURSEMENT_AI_ATTACHMENT_DUPLICATE = new ErrorCode(1_070_000_019, "AI 附件冲突");
    /** 邮件时间筛选条件不合法。 */
    ErrorCode REIMBURSEMENT_MAIL_FILTER_INVALID = new ErrorCode(1_070_000_021, "邮件时间筛选条件不合法");
}
