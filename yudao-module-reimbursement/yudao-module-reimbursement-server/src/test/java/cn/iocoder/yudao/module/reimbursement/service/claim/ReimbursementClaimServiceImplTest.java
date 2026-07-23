package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimSubmitReqVO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimUpdateReqVO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementItemReqVO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementAttachmentDO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementItemDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementAttachmentMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementClaimMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementItemMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import org.junit.jupiter.api.Test;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;

import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_CLAIM_DELETE_STATUS_INVALID;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_CLAIM_NOT_EXISTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

class ReimbursementClaimServiceImplTest {

    @Test
    void getAttachmentAccessUrlFallsBackForUnsupportedFileClient() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementAttachmentMapper attachmentMapper = mock(ReimbursementAttachmentMapper.class);
        SecurityFrameworkService securityFrameworkService = mock(SecurityFrameworkService.class);
        FileApi fileApi = mock(FileApi.class);
        ReimbursementClaimDO claim = new ReimbursementClaimDO();
        claim.setId(5L);
        claim.setUserId(10L);
        ReimbursementAttachmentDO attachment = new ReimbursementAttachmentDO();
        attachment.setId(7L);
        attachment.setReimbursementId(5L);
        attachment.setFileUrl("http://localhost/admin-api/infra/file/4/get/reimbursement/invoice.pdf");
        when(claimMapper.selectByIdForUser(5L, 10L, false)).thenReturn(claim);
        when(attachmentMapper.selectById(7L)).thenReturn(attachment);
        when(fileApi.presignGetUrl(attachment.getFileUrl(), 300))
                .thenThrow(new UnsupportedOperationException("不支持的操作"));
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper,
                mock(ReimbursementItemMapper.class), attachmentMapper, mock(AdminUserApi.class),
                mock(BpmProcessInstanceApi.class), securityFrameworkService, fileApi);

        String result = service.getAttachmentAccessUrl(10L, 5L, 7L);

        assertEquals(attachment.getFileUrl(), result);
    }

    @Test
    void submitClaimShouldNotStartBpmTwiceWhenAlreadySubmitted() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementClaimDO submittedClaim = new ReimbursementClaimDO();
        submittedClaim.setId(1L);
        submittedClaim.setUserId(10L);
        submittedClaim.setStatus(ReimbursementStatusEnum.SUBMITTED.getStatus());
        submittedClaim.setProcessInstanceId("process-1");
        when(claimMapper.selectOwnedById(1L, 10L)).thenReturn(submittedClaim);
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper,
                mock(ReimbursementItemMapper.class), mock(ReimbursementAttachmentMapper.class),
                mock(AdminUserApi.class), mock(BpmProcessInstanceApi.class),
                mock(SecurityFrameworkService.class), mock(FileApi.class));
        ReimbursementClaimSubmitReqVO request = new ReimbursementClaimSubmitReqVO();
        request.setId(1L);

        var result = service.submitClaim(10L, request);
        assertEquals(1L, result.getReimbursementId());
        assertEquals("process-1", result.getProcessInstanceId());
        verify(claimMapper, never()).updateById(any(ReimbursementClaimDO.class));
    }

    @Test
    void updateClaimAllowsReusingClientItemIdAfterPhysicalDelete() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementItemMapper itemMapper = mock(ReimbursementItemMapper.class);
        ReimbursementAttachmentMapper attachmentMapper = mock(ReimbursementAttachmentMapper.class);
        ReimbursementClaimDO draft = new ReimbursementClaimDO();
        draft.setId(2L);
        draft.setUserId(10L);
        draft.setStatus(ReimbursementStatusEnum.DRAFT.getStatus());
        when(claimMapper.selectOwnedById(2L, 10L)).thenReturn(draft);
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper, itemMapper,
                attachmentMapper, mock(AdminUserApi.class), mock(BpmProcessInstanceApi.class),
                mock(SecurityFrameworkService.class), mock(FileApi.class));
        TenantContextHolder.setTenantId(1L);
        try {
            ReimbursementClaimUpdateReqVO request = updateRequest(2L, "item-1");
            service.updateClaim(10L, request);
            service.updateClaim(10L, request);
            verify(itemMapper, times(2)).deletePermanentlyByReimbursementId(2L, 1L);
            verify(itemMapper, times(2)).insert(any(ReimbursementItemDO.class));
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Test
    void updateClaimClearsAttachmentLinksBeforeDeletingItems() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementItemMapper itemMapper = mock(ReimbursementItemMapper.class);
        ReimbursementAttachmentMapper attachmentMapper = mock(ReimbursementAttachmentMapper.class);
        ReimbursementClaimDO draft = new ReimbursementClaimDO();
        draft.setId(3L);
        draft.setUserId(10L);
        draft.setStatus(ReimbursementStatusEnum.DRAFT.getStatus());
        when(claimMapper.selectOwnedById(3L, 10L)).thenReturn(draft);
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper, itemMapper,
                attachmentMapper, mock(AdminUserApi.class), mock(BpmProcessInstanceApi.class),
                mock(SecurityFrameworkService.class), mock(FileApi.class));
        TenantContextHolder.setTenantId(1L);
        try {
            service.updateClaim(10L, updateRequest(3L, "item-1"));
            InOrder order = inOrder(attachmentMapper, itemMapper);
            order.verify(attachmentMapper).clearItemIdByReimbursementId(3L, 1L);
            order.verify(itemMapper).deletePermanentlyByReimbursementId(3L, 1L);
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Test
    void deleteClaimRemovesDraftAndItsRelatedData() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementItemMapper itemMapper = mock(ReimbursementItemMapper.class);
        ReimbursementAttachmentMapper attachmentMapper = mock(ReimbursementAttachmentMapper.class);
        ReimbursementClaimDO draft = new ReimbursementClaimDO();
        draft.setId(4L);
        draft.setUserId(10L);
        draft.setStatus(ReimbursementStatusEnum.DRAFT.getStatus());
        when(claimMapper.selectOwnedById(4L, 10L)).thenReturn(draft);
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper, itemMapper,
                attachmentMapper, mock(AdminUserApi.class), mock(BpmProcessInstanceApi.class),
                mock(SecurityFrameworkService.class), mock(FileApi.class));
        TenantContextHolder.setTenantId(1L);
        try {
            service.deleteClaim(10L, 4L);
            InOrder order = inOrder(attachmentMapper, itemMapper, claimMapper);
            order.verify(attachmentMapper).clearItemIdByReimbursementId(4L, 1L);
            order.verify(attachmentMapper).deleteByReimbursementId(4L, 1L);
            order.verify(itemMapper).deletePermanentlyByReimbursementId(4L, 1L);
            order.verify(claimMapper).deleteById(4L);
        } finally {
            TenantContextHolder.clear();
        }
    }

    @Test
    void deleteClaimRejectsSubmittedClaim() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        ReimbursementClaimDO submitted = new ReimbursementClaimDO();
        submitted.setId(5L);
        submitted.setUserId(10L);
        submitted.setStatus(ReimbursementStatusEnum.SUBMITTED.getStatus());
        when(claimMapper.selectOwnedById(5L, 10L)).thenReturn(submitted);
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper,
                mock(ReimbursementItemMapper.class), mock(ReimbursementAttachmentMapper.class),
                mock(AdminUserApi.class), mock(BpmProcessInstanceApi.class),
                mock(SecurityFrameworkService.class), mock(FileApi.class));

        assertServiceException(() -> service.deleteClaim(10L, 5L),
                REIMBURSEMENT_CLAIM_DELETE_STATUS_INVALID);

        verify(claimMapper, never()).deleteById(any());
    }

    @Test
    void deleteClaimRejectsClaimOwnedByAnotherUser() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        when(claimMapper.selectOwnedById(6L, 10L)).thenReturn(null);
        ReimbursementClaimServiceImpl service = new ReimbursementClaimServiceImpl(claimMapper,
                mock(ReimbursementItemMapper.class), mock(ReimbursementAttachmentMapper.class),
                mock(AdminUserApi.class), mock(BpmProcessInstanceApi.class),
                mock(SecurityFrameworkService.class), mock(FileApi.class));

        assertServiceException(() -> service.deleteClaim(10L, 6L), REIMBURSEMENT_CLAIM_NOT_EXISTS);

        verify(claimMapper, never()).deleteById(any());
    }

    private static ReimbursementClaimUpdateReqVO updateRequest(Long id, String clientItemId) {
        ReimbursementItemReqVO item = new ReimbursementItemReqVO();
        item.setClientItemId(clientItemId);
        item.setExpenseDate(LocalDate.of(2026, 7, 21));
        item.setExpenseType("OTHER");
        item.setAmount(new BigDecimal("12.34"));
        ReimbursementClaimUpdateReqVO request = new ReimbursementClaimUpdateReqVO();
        request.setId(id);
        request.setReason("测试报销");
        request.setCurrency("CNY");
        request.setItems(List.of(item));
        return request;
    }

}
