package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport;

import lombok.Data;

/**
 * 邮件导入启动结果。
 */

@Data
public class ReimbursementMailImportStartRespVO {
    /** 报销单编号。 */
    private Long reimbursementId;
    /** 状态。 */
    private Integer status;
    /** 提交模式。 */
    private String submitMode;
}
