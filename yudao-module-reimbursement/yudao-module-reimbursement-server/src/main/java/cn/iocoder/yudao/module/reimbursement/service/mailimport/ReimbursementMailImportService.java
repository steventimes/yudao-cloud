package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.*;

public interface ReimbursementMailImportService {
    ReimbursementMailImportStartRespVO start(Long userId, ReimbursementMailImportStartReqVO reqVO);
}
