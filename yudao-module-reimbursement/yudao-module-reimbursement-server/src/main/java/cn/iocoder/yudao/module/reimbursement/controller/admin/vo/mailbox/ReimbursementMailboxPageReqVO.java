package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 报销邮箱连接分页查询请求。
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ReimbursementMailboxPageReqVO extends PageParam {
    private String providerCode;
    private String email;
    /** 状态。 */
    private Integer status;
}
