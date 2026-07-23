package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import lombok.Data;

/**
 * 邮箱连接验证结果。
 */

@Data
public class ReimbursementMailboxVerifyRespVO {
    /** 编号。 */
    private Long id;
    /** 状态。 */
    private Integer status;
    private String message;
}
