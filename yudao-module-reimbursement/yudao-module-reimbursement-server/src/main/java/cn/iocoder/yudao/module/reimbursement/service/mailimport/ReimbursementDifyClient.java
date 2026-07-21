package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import java.time.LocalDate;

/**
 * 报销 Dify Workflow 客户端
 * 
 * @author Codex
 */
public interface ReimbursementDifyClient {

    void requireConfigured();

    /**
     * 执行 run 业务操作。
     * 
     * @param request Dify 工作流执行请求
     */
    ReimbursementDifyRunResult run(ReimbursementDifyRunRequest request);

    /**
     * Dify 工作流执行请求参数。
     * 
     * @param tenantId        租户编号
     * @param reimbursementId 报销单编号
     * @param mailAccessToken 邮箱访问令牌
     * @param folder          邮箱文件夹
     * @param lookbackDays    回溯天数
     * @param fromDate        起始日期
     * @param toDate          结束日期
     * @param unreadOnly      是否仅处理未读邮件
     * @param subjectKeywords 主题关键词
     * @param senderContains  发件人过滤关键字
     * @param maxMessages     最大处理邮件数
     * @param userId          用户编号
     */
    record ReimbursementDifyRunRequest(Long tenantId, Long reimbursementId, String mailAccessToken,
            String folder, Integer lookbackDays, LocalDate fromDate, LocalDate toDate,
            Boolean unreadOnly, String subjectKeywords, String senderContains,
            Integer maxMessages, Long userId) {
    }

    /**
     * Dify 工作流执行结果。
     * 
     * @param workflowRunId 工作流运行编号
     */
    record ReimbursementDifyRunResult(String workflowRunId) {
    }

}
