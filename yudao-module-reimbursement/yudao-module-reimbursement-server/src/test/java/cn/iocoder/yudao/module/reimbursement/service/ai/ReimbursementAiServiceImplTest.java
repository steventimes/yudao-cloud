package cn.iocoder.yudao.module.reimbursement.service.ai;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.ReimbursementAiFillReqVO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementAttachmentMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementClaimMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementItemMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ReimbursementAiServiceImplTest extends AbstractReimbursementUnitTest {

    @Test
    void applyAiFillShouldRejectMissingClaim() {
        ReimbursementClaimMapper claimMapper = mock(ReimbursementClaimMapper.class);
        when(claimMapper.selectById(1L)).thenReturn(null);
        ReimbursementAiServiceImpl service = new ReimbursementAiServiceImpl(mock(FileApi.class), newProperties(),
                claimMapper,
                mock(ReimbursementItemMapper.class), mock(ReimbursementAttachmentMapper.class),
                mock(ReimbursementClaimService.class));
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
                mock(ReimbursementClaimService.class));
        ReimbursementAiFillReqVO request = new ReimbursementAiFillReqVO();
        request.setReimbursementId(1L);

        service.applyAiFill(1L, request);
        verify(claimMapper, never()).updateById(any(ReimbursementClaimDO.class));
    }

}
