package cn.iocoder.yudao.module.reimbursement.service.ai;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimSubmitRespVO;
import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.ReimbursementAiFillRespVO;
import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.ReimbursementAiFillReqVO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementAttachmentMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementClaimMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementItemMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementAiSubmitModeEnum;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementSourceEnum;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReimbursementAiServiceImplTest extends AbstractReimbursementUnitTest {

    @Test
    void applyAiFillShouldRejectMissingClaim() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        when(claimMapper.selectById(1L)).thenReturn(null);
        ReimbursementAiServiceImpl service = new ReimbursementAiServiceImpl(mock(FileApi.class), newProperties(),
                claimMapper,
                mock(ReimbursementItemMapper.class), mock(ReimbursementAttachmentMapper.class),
                mock(ReimbursementClaimService.class), mock(TransactionTemplate.class));
        ReimbursementAiFillReqVO request = new ReimbursementAiFillReqVO();
        request.setReimbursementId(1L);

        assertThrows(RuntimeException.class, () -> service.applyAiFill(1L, request));
    }

    @Test
    void applyAiFillShouldIgnoreDuplicateSubmittedClaim() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementClaimDO claim = new ReimbursementClaimDO();
        claim.setId(1L);
        claim.setStatus(ReimbursementStatusEnum.SUBMITTED.getStatus());
        claim.setSource(cn.iocoder.yudao.module.reimbursement.enums.ReimbursementSourceEnum.AI_EMAIL.name());
        when(claimMapper.selectById(1L)).thenReturn(claim);
        ReimbursementAiServiceImpl service = new ReimbursementAiServiceImpl(mock(FileApi.class), newProperties(),
                claimMapper,
                mock(ReimbursementItemMapper.class), mock(ReimbursementAttachmentMapper.class),
                mock(ReimbursementClaimService.class), mock(TransactionTemplate.class));
        ReimbursementAiFillReqVO request = new ReimbursementAiFillReqVO();
        request.setReimbursementId(1L);

        service.applyAiFill(1L, request);
        verify(claimMapper, never()).updateById(any(ReimbursementClaimDO.class));
    }

    @Test
    void applyAiFillShouldAutoSubmitSuccessfully() {
        ReimbursementProperties properties = newProperties();
        properties.getAi().setSubmitMode(ReimbursementAiSubmitModeEnum.AUTO_SUBMIT);
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementItemMapper itemMapper = mock(ReimbursementItemMapper.class);
        ReimbursementClaimService claimService = mock(ReimbursementClaimService.class);
        ReimbursementClaimDO claim = aiProcessingClaim();
        when(claimMapper.selectById(1L)).thenReturn(claim);
        ReimbursementClaimSubmitRespVO submitResponse = new ReimbursementClaimSubmitRespVO();
        submitResponse.setStatus(ReimbursementStatusEnum.SUBMITTED.getStatus());
        submitResponse.setProcessInstanceId("process-1");
        when(claimService.autoSubmitClaim(1L, 2003L, 1L)).thenReturn(submitResponse);
        ReimbursementAiServiceImpl service = new ReimbursementAiServiceImpl(mock(FileApi.class), properties,
                claimMapper, itemMapper, mock(ReimbursementAttachmentMapper.class), claimService,
                passthroughTransactionTemplate());

        ReimbursementAiFillRespVO result = service.applyAiFill(1L, validAiFillRequest());

        assertTrue(result.getAutoSubmitAttempted());
        assertTrue(result.getAutoSubmitSucceeded());
        assertEquals(ReimbursementStatusEnum.SUBMITTED.getStatus(), result.getStatus());
        assertEquals("process-1", result.getProcessInstanceId());
        verify(claimService).autoSubmitClaim(1L, 2003L, 1L);
    }

    @Test
    void applyAiFillShouldKeepPendingDraftWhenAutoSubmitFails() {
        ReimbursementProperties properties = newProperties();
        properties.getAi().setSubmitMode(ReimbursementAiSubmitModeEnum.AUTO_SUBMIT);
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementItemMapper itemMapper = mock(ReimbursementItemMapper.class);
        ReimbursementClaimService claimService = mock(ReimbursementClaimService.class);
        ReimbursementClaimDO claim = aiProcessingClaim();
        when(claimMapper.selectById(1L)).thenReturn(claim);
        doThrow(new RuntimeException("BPM unavailable"))
                .when(claimService).autoSubmitClaim(1L, 2003L, 1L);
        ReimbursementAiServiceImpl service = new ReimbursementAiServiceImpl(mock(FileApi.class), properties,
                claimMapper, itemMapper, mock(ReimbursementAttachmentMapper.class), claimService,
                passthroughTransactionTemplate());

        ReimbursementAiFillRespVO result = service.applyAiFill(1L, validAiFillRequest());

        assertTrue(result.getAutoSubmitAttempted());
        assertFalse(result.getAutoSubmitSucceeded());
        assertEquals(ReimbursementStatusEnum.PENDING_CONFIRMATION.getStatus(), result.getStatus());
        assertNull(result.getProcessInstanceId());
    }

    private static ReimbursementClaimDO aiProcessingClaim() {
        ReimbursementClaimDO claim = new ReimbursementClaimDO();
        claim.setId(1L);
        claim.setUserId(2003L);
        claim.setStatus(ReimbursementStatusEnum.AI_PROCESSING.getStatus());
        claim.setSource(ReimbursementSourceEnum.AI_EMAIL.name());
        return claim;
    }

    private static ReimbursementAiFillReqVO validAiFillRequest() {
        ReimbursementAiFillReqVO request = new ReimbursementAiFillReqVO();
        request.setTenantId(1L);
        request.setReimbursementId(1L);
        request.setReason("AUTO_SUBMIT 测试");
        request.setCurrency("CNY");
        request.setAiConfidence(new BigDecimal("0.90"));
        ReimbursementAiFillReqVO.Item item = new ReimbursementAiFillReqVO.Item();
        item.setClientItemId("item-1");
        item.setExpenseDate(LocalDate.of(2026, 7, 23));
        item.setExpenseType("TRANSPORT");
        item.setMerchantName("测试商户");
        item.setAmount(new BigDecimal("10.00"));
        item.setTaxAmount(new BigDecimal("1.00"));
        item.setAiConfidence(new BigDecimal("0.80"));
        item.setAttachmentExternalArtifactIds(List.of());
        request.setItems(List.of(item));
        return request;
    }

    @SuppressWarnings("unchecked")
    private static TransactionTemplate passthroughTransactionTemplate() {
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<Object> callback = invocation.getArgument(0);
            return callback.doInTransaction(mock(TransactionStatus.class));
        });
        return transactionTemplate;
    }

}
