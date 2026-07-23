package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;

/**
 * AI 识别结果回填响应。
 */

@Data
public class ReimbursementAiFillRespVO {
    /** 报销单编号。 */
    private Long reimbursementId;
    /** 状态。 */
    private Integer status;
    /** 提交模式。 */
    private String submitMode;
    private Boolean autoSubmitAttempted;
    private Boolean autoSubmitSucceeded;
    /** 流程实例编号。 */
    private String processInstanceId;
    private String message;
}
