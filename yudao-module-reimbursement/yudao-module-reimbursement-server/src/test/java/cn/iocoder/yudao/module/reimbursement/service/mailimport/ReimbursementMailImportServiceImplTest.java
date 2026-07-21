package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.ReimbursementMailImportStartReqVO;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import cn.iocoder.yudao.module.reimbursement.service.mailbox.ReimbursementMailboxService;
import org.junit.jupiter.api.Test;
import org.springframework.core.task.SyncTaskExecutor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

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

        assertThrows(RuntimeException.class, () -> service.start(10L, request));
    }

}
