package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.ReimbursementMailImportStartReqVO;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import cn.iocoder.yudao.module.reimbursement.service.mailbox.ReimbursementMailboxService;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAIL_FILTER_INVALID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReimbursementMailImportServiceImplTest {

    @Test
    void startImportShouldRejectInvalidDateRange() {
        ReimbursementMailImportServiceImpl service = new ReimbursementMailImportServiceImpl(new SyncTaskExecutor(),
                mock(ReimbursementDifyClient.class),
                mock(ReimbursementMailboxService.class), mock(ReimbursementClaimService.class),
                mock(ReimbursementMailAccessGrantService.class),
                new cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties());
        ReimbursementMailImportStartReqVO request = new ReimbursementMailImportStartReqVO();
        request.setMailboxConnectionId(1L);
        request.setFromDate(LocalDate.of(2026, 7, 20));
        request.setLookbackDays(7);

        assertServiceException(() -> service.start(10L, request), REIMBURSEMENT_MAIL_FILTER_INVALID);
    }

    @Test
    void startImportShouldRejectReversedDateRange() {
        ReimbursementMailImportServiceImpl service = new ReimbursementMailImportServiceImpl(new SyncTaskExecutor(),
                mock(ReimbursementDifyClient.class),
                mock(ReimbursementMailboxService.class), mock(ReimbursementClaimService.class),
                mock(ReimbursementMailAccessGrantService.class),
                new cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties());
        ReimbursementMailImportStartReqVO request = new ReimbursementMailImportStartReqVO();
        request.setMailboxConnectionId(1L);
        request.setLookbackDays(null);
        request.setFromDate(LocalDate.of(2026, 7, 21));
        request.setToDate(LocalDate.of(2026, 7, 20));

        assertServiceException(() -> service.start(10L, request), REIMBURSEMENT_MAIL_FILTER_INVALID);
    }

    @Test
    void startImportShouldDefaultLookbackWhenNoTimeFilterProvided() {
        ReimbursementDifyClient difyClient = mock(ReimbursementDifyClient.class);
        ReimbursementClaimService claimService = mock(ReimbursementClaimService.class);
        ReimbursementMailAccessGrantService grantService = mock(ReimbursementMailAccessGrantService.class);
        when(claimService.createAiProcessingClaim(10L, 1L)).thenReturn(9L);
        when(grantService.issue(1L, 10L, 1L)).thenReturn("mail-access-token");
        ReimbursementMailImportServiceImpl service = new ReimbursementMailImportServiceImpl(new SyncTaskExecutor(),
                difyClient, mock(ReimbursementMailboxService.class), claimService, grantService,
                new cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties());
        ReimbursementMailImportStartReqVO request = new ReimbursementMailImportStartReqVO();
        request.setMailboxConnectionId(1L);
        request.setMaxMessages(1);

        TenantContextHolder.setTenantId(1L);
        try {
            service.start(10L, request);
        } finally {
            TenantContextHolder.clear();
        }

        var captor = org.mockito.ArgumentCaptor.forClass(
                ReimbursementDifyClient.ReimbursementDifyRunRequest.class);
        verify(difyClient).run(captor.capture());
        assertEquals(30, captor.getValue().lookbackDays());
    }

    @Test
    void startImportShouldMarkProcessingClaimFailedWhenDifyThrows() {
        ReimbursementDifyClient difyClient = mock(ReimbursementDifyClient.class);
        ReimbursementClaimService claimService = mock(ReimbursementClaimService.class);
        ReimbursementMailAccessGrantService grantService = mock(ReimbursementMailAccessGrantService.class);
        when(claimService.createAiProcessingClaim(10L, 1L)).thenReturn(7L);
        when(grantService.issue(1L, 10L, 1L)).thenReturn("mail-access-token");
        doThrow(new RuntimeException("Dify unavailable")).when(difyClient).run(any());
        ReimbursementMailImportServiceImpl service = new ReimbursementMailImportServiceImpl(new SyncTaskExecutor(),
                difyClient, mock(ReimbursementMailboxService.class), claimService, grantService,
                new cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties());

        TenantContextHolder.setTenantId(1L);
        try {
            service.start(10L, validRequest());
        } finally {
            TenantContextHolder.clear();
        }

        verify(claimService).markAiFailedIfProcessing(1L, 7L, "Dify workflow failed");
    }

    @Test
    void startImportShouldMarkProcessingClaimFailedWhenWorkflowDoesNotFill() {
        ReimbursementDifyClient difyClient = mock(ReimbursementDifyClient.class);
        ReimbursementClaimService claimService = mock(ReimbursementClaimService.class);
        ReimbursementMailAccessGrantService grantService = mock(ReimbursementMailAccessGrantService.class);
        when(claimService.createAiProcessingClaim(10L, 1L)).thenReturn(8L);
        when(grantService.issue(1L, 10L, 1L)).thenReturn("mail-access-token");
        when(difyClient.run(any())).thenReturn(new ReimbursementDifyClient.ReimbursementDifyRunResult("run-1"));
        ReimbursementMailImportServiceImpl service = new ReimbursementMailImportServiceImpl(new SyncTaskExecutor(),
                difyClient, mock(ReimbursementMailboxService.class), claimService, grantService,
                new cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties());

        TenantContextHolder.setTenantId(1L);
        try {
            service.start(10L, validRequest());
        } finally {
            TenantContextHolder.clear();
        }

        verify(claimService).markAiFailedIfProcessing(1L, 8L,
                "Dify workflow completed without ai-fill");
    }

    private static ReimbursementMailImportStartReqVO validRequest() {
        ReimbursementMailImportStartReqVO request = new ReimbursementMailImportStartReqVO();
        request.setMailboxConnectionId(1L);
        request.setLookbackDays(30);
        request.setMaxMessages(1);
        return request;
    }

}
