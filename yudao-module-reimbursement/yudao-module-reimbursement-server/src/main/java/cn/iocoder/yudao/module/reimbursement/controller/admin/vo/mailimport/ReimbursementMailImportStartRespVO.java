package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport;

import lombok.Data;

@Data
public class ReimbursementMailImportStartRespVO {
    private Long reimbursementId;
    private Integer status;
    private String submitMode;
}
