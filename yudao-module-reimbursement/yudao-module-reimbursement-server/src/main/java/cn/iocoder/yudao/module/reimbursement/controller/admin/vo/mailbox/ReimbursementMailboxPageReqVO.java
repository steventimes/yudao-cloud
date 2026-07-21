package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ReimbursementMailboxPageReqVO extends PageParam {
    private String providerCode;
    private String email;
    private Integer status;
}
