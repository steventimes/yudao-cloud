package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;

public interface ReimbursementClaimService {
    Long createClaim(Long userId, ReimbursementClaimCreateReqVO reqVO);

    void updateClaim(Long userId, ReimbursementClaimUpdateReqVO reqVO);

    void confirmClaim(Long userId, ReimbursementClaimConfirmReqVO reqVO);

    ReimbursementClaimSubmitRespVO submitClaim(Long userId, ReimbursementClaimSubmitReqVO reqVO);

    ReimbursementClaimRespVO getClaim(Long userId, Long id);

    PageResult<ReimbursementClaimDO> getClaimPage(Long userId, ReimbursementClaimPageReqVO reqVO);

    String getAttachmentAccessUrl(Long userId, Long reimbursementId, Long attachmentId);

    Long createAiProcessingClaim(Long userId, Long mailboxConnectionId);

    void markAiFailedIfProcessing(Long tenantId, Long reimbursementId, String failureMessage);

    ReimbursementClaimSubmitRespVO autoSubmitClaim(Long tenantId, Long ownerUserId, Long reimbursementId);
}
