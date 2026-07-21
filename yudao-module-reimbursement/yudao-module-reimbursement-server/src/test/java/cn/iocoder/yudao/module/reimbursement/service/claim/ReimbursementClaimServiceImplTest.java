package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimSubmitReqVO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementAttachmentMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementClaimMapper;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementItemMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementStatusEnum;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.bpm.api.task.BpmProcessInstanceApi;
import org.junit.jupiter.api.Test;
import cn.iocoder.yudao.framework.security.core.service.SecurityFrameworkService;
import cn.iocoder.yudao.module.infra.api.file.FileApi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReimbursementClaimServiceImplTest {

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

        assertEquals("process-1", service.submitClaim(10L, request).getProcessInstanceId());
        verify(claimMapper, never()).updateById(any(ReimbursementClaimDO.class));
    }

}
