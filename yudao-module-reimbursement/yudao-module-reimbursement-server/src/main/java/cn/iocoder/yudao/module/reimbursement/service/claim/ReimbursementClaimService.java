package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;

/**
 * ReimbursementClaimService，业务服务。
 */
public interface ReimbursementClaimService {
    /**
     * 执行 createClaim 业务操作。
     * 
     * @param userId 用户编号
     * @param reqVO  请求参数对象
     */
    Long createClaim(Long userId, ReimbursementClaimCreateReqVO reqVO);

    void updateClaim(Long userId, ReimbursementClaimUpdateReqVO reqVO);

    void confirmClaim(Long userId, ReimbursementClaimConfirmReqVO reqVO);

    /**
     * 执行 submitClaim 业务操作。
     * 
     * @param userId 用户编号
     * @param reqVO  请求参数对象
     */
    ReimbursementClaimSubmitRespVO submitClaim(Long userId, ReimbursementClaimSubmitReqVO reqVO);

    /**
     * 执行 getClaim 业务操作。
     * 
     * @param userId 用户编号
     * @param id     记录编号
     */
    ReimbursementClaimRespVO getClaim(Long userId, Long id);

    /**
     * 执行 getClaimPage 业务操作。
     * 
     * @param userId 用户编号
     * @param reqVO  请求参数对象
     */
    PageResult<ReimbursementClaimDO> getClaimPage(Long userId, ReimbursementClaimPageReqVO reqVO);

    /**
     * 执行 getAttachmentAccessUrl 业务操作。
     * 
     * @param userId          用户编号
     * @param reimbursementId 报销单编号
     * @param attachmentId    附件编号
     */
    String getAttachmentAccessUrl(Long userId, Long reimbursementId, Long attachmentId);

    /**
     * 执行 createAiProcessingClaim 业务操作。
     * 
     * @param userId              用户编号
     * @param mailboxConnectionId 邮箱连接编号
     */
    Long createAiProcessingClaim(Long userId, Long mailboxConnectionId);

    void markAiFailedIfProcessing(Long tenantId, Long reimbursementId, String failureMessage);

    /**
     * 执行 autoSubmitClaim 业务操作。
     * 
     * @param tenantId        租户编号
     * @param ownerUserId     邮箱绑定所属用户编号
     * @param reimbursementId 报销单编号
     */
    ReimbursementClaimSubmitRespVO autoSubmitClaim(Long tenantId, Long ownerUserId, Long reimbursementId);
}
