package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.*;

/**
 * 邮件票据导入服务。
 */
public interface ReimbursementMailImportService {
    /** 创建 AI 处理中的报销单、签发短期邮箱令牌并异步启动 Dify Workflow。 */
    ReimbursementMailImportStartRespVO start(Long userId, ReimbursementMailImportStartReqVO reqVO);
}
