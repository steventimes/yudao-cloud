package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.*;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import cn.iocoder.yudao.module.reimbursement.service.mailbox.ReimbursementMailboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_ITEM_INVALID;

/**
 * 报销邮件导入 Service 实现类
 * 
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class ReimbursementMailImportServiceImpl implements ReimbursementMailImportService {

    private final TaskExecutor applicationTaskExecutor;
    private final ReimbursementDifyClient difyClient;
    private final ReimbursementMailboxService mailboxService;
    private final ReimbursementClaimService claimService;
    private final ReimbursementMailAccessGrantService grantService;
    private final ReimbursementProperties reimbursementProperties;

    /**
     * 启动邮箱导入。
     * 
     * @param userId     用户编号
     * @param startReqVO 邮件导入启动参数
     * @return 处理结果
     */

    @Override
    public ReimbursementMailImportStartRespVO start(Long userId, ReimbursementMailImportStartReqVO startReqVO) {
        validateDateRange(startReqVO);
        difyClient.requireConfigured();
        mailboxService.requireVerifiedOwnedMailbox(userId, startReqVO.getMailboxConnectionId());
        Long reimbursementId = claimService.createAiProcessingClaim(userId, startReqVO.getMailboxConnectionId());
        Long tenantId = TenantContextHolder.getRequiredTenantId();
        String mailAccessToken = grantService.issue(tenantId, userId, startReqVO.getMailboxConnectionId());
        ReimbursementDifyClient.ReimbursementDifyRunRequest difyRunRequest = buildDifyRunRequest(
                tenantId, userId, reimbursementId, mailAccessToken, startReqVO);
        applicationTaskExecutor.execute(() -> runDifyWorkflow(tenantId, reimbursementId, difyRunRequest));

        ReimbursementMailImportStartRespVO startRespVO = new ReimbursementMailImportStartRespVO();
        startRespVO.setReimbursementId(reimbursementId);
        startRespVO.setStatus(ReimbursementStatusEnum.AI_PROCESSING.getStatus());
        startRespVO.setSubmitMode(reimbursementProperties.getAi().getSubmitMode().name());
        return startRespVO;
    }

    /**
     * 校验DateRange参数。
     * 
     * @param startReqVO 邮件导入启动参数
     */
    private void validateDateRange(ReimbursementMailImportStartReqVO startReqVO) {
        boolean hasLookbackDays = startReqVO.getLookbackDays() != null;
        boolean hasDateRange = startReqVO.getFromDate() != null || startReqVO.getToDate() != null;
        if (hasLookbackDays && hasDateRange) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_ITEM_INVALID);
        }
    }

    private ReimbursementDifyClient.ReimbursementDifyRunRequest buildDifyRunRequest(
            Long tenantId, Long userId, Long reimbursementId, String mailAccessToken,
            ReimbursementMailImportStartReqVO startReqVO) {
        return new ReimbursementDifyClient.ReimbursementDifyRunRequest(tenantId, reimbursementId, mailAccessToken,
                startReqVO.getFolder(), startReqVO.getLookbackDays(), startReqVO.getFromDate(), startReqVO.getToDate(),
                startReqVO.getUnreadOnly(), startReqVO.getSubjectKeywords(), startReqVO.getSenderContains(),
                startReqVO.getMaxMessages(), userId);
    }

    private void runDifyWorkflow(Long tenantId, Long reimbursementId,
            ReimbursementDifyClient.ReimbursementDifyRunRequest difyRunRequest) {
        TenantUtils.execute(tenantId, () -> {
            try {
                difyClient.run(difyRunRequest);
                claimService.markAiFailedIfProcessing(tenantId, reimbursementId,
                        "Dify workflow completed without ai-fill");
            } catch (Exception ex) {
                claimService.markAiFailedIfProcessing(tenantId, reimbursementId, "Dify workflow failed");
            }
        });
    }

}
