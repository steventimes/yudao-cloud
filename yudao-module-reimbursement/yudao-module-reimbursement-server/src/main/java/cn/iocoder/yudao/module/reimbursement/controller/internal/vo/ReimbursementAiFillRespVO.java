package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;

@Data
public class ReimbursementAiFillRespVO {
    private Long reimbursementId;
    private Integer status;
    private String submitMode;
    private Boolean autoSubmitAttempted;
    private Boolean autoSubmitSucceeded;
    private String processInstanceId;
    private String message;
}
