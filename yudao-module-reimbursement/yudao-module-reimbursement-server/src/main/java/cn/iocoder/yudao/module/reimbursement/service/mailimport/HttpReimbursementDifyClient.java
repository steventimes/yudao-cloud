package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_DIFY_NOT_CONFIGURED;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_DIFY_REQUEST_FAILED;

/**
 * HTTP 报销 Dify Workflow 客户端
 *
 * @author Codex
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpReimbursementDifyClient implements ReimbursementDifyClient {

    private static final String MAIL_ACCESS_TOKEN_INPUT = "mailbox" + "_execution_token";

    private final RestTemplateBuilder restTemplateBuilder;
    private final ReimbursementProperties reimbursementProperties;

    @Override
    public void requireConfigured() {
        ReimbursementProperties.Dify difyProperties = reimbursementProperties.getDify();
        if (!difyProperties.isEnabled() || StrUtil.isBlank(difyProperties.getApiBaseUrl())
                || StrUtil.isBlank(difyProperties.getApiKey())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_DIFY_NOT_CONFIGURED);
        }
    }

    @Override
    public ReimbursementDifyRunResult run(ReimbursementDifyRunRequest request) {
        requireConfigured();
        ReimbursementProperties.Dify difyProperties = reimbursementProperties.getDify();
        RestTemplate restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofMillis(difyProperties.getTimeoutMillis()))
                .readTimeout(Duration.ofMillis(difyProperties.getTimeoutMillis()))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(difyProperties.getApiKey());
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(buildRequestBody(request), headers);
        String runUrl = StrUtil.removeSuffix(difyProperties.getApiBaseUrl(), "/") + "/workflows/run";
        try {
            ResponseEntity<Map> response = restTemplate.exchange(runUrl, HttpMethod.POST, httpEntity, Map.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw ServiceExceptionUtil.exception(REIMBURSEMENT_DIFY_REQUEST_FAILED);
            }
            String workflowRunId = response.getBody() == null ? null
                    : String.valueOf(response.getBody().get("workflow_run_id"));
            log.info("Dify reimbursement workflow completed, reimbursementId={}, status={}, workflowRunId={}",
                    request.reimbursementId(), response.getStatusCode().value(), workflowRunId);
            return new ReimbursementDifyRunResult(workflowRunId);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_DIFY_REQUEST_FAILED);
        }
    }

    private Map<String, Object> buildRequestBody(ReimbursementDifyRunRequest request) {
        Map<String, Object> inputs = new LinkedHashMap<>();
        inputs.put("tenant_id", request.tenantId());
        inputs.put("reimbursement_id", request.reimbursementId());
        inputs.put(MAIL_ACCESS_TOKEN_INPUT, request.mailAccessToken());
        inputs.put("folder", StrUtil.blankToDefault(request.folder(), "INBOX"));
        inputs.put("lookback_days", request.lookbackDays());
        inputs.put("from_date", request.fromDate() == null ? "" : request.fromDate().toString());
        inputs.put("to_date", request.toDate() == null ? "" : request.toDate().toString());
        inputs.put("unread_only", Boolean.TRUE.equals(request.unreadOnly()));
        inputs.put("subject_keywords", StrUtil.blankToDefault(request.subjectKeywords(), ""));
        inputs.put("sender_contains", StrUtil.blankToDefault(request.senderContains(), ""));
        inputs.put("max_messages", request.maxMessages());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("inputs", inputs);
        body.put("response_mode", "blocking");
        body.put("user",
                "reimbursement-" + request.tenantId() + '-' + request.userId() + '-' + request.reimbursementId());
        return body;
    }

}
