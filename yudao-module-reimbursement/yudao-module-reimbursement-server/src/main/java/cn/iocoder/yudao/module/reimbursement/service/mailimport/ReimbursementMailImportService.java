package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.*;

/**
 * ReimbursementMailImportService，业务服务。
 */
public interface ReimbursementMailImportService {
    /**
     * 执行 start 业务操作。
     * 
     * @param userId 用户编号
     * @param reqVO  请求参数对象
     */
    ReimbursementMailImportStartRespVO start(Long userId, ReimbursementMailImportStartReqVO reqVO);
}
