package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import java.time.LocalDate;

/**
 * 报销 Dify Workflow 客户端
 *
 * @author Codex
 */
public interface ReimbursementDifyClient {

    void requireConfigured();

    ReimbursementDifyRunResult run(ReimbursementDifyRunRequest request);

    record ReimbursementDifyRunRequest(Long tenantId, Long reimbursementId, String mailAccessToken,
            String folder, Integer lookbackDays, LocalDate fromDate, LocalDate toDate,
            Boolean unreadOnly, String subjectKeywords, String senderContains,
            Integer maxMessages, Long userId) {
    }

    record ReimbursementDifyRunResult(String workflowRunId) {
    }

}
