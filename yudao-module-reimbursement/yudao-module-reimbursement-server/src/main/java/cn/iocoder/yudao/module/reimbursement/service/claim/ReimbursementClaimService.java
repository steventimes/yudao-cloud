package cn.iocoder.yudao.module.reimbursement.service.claim;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;

/**
 * 报销单业务服务，负责所有权校验、状态流转和 BPM 提交。
 */
public interface ReimbursementClaimService {
    /** 创建人工报销草稿并返回报销单编号。 */
    Long createClaim(Long userId, ReimbursementClaimCreateReqVO reqVO);

    /** 更新草稿、AI 待确认或 AI 失败报销单，并将其恢复为草稿。 */
    void updateClaim(Long userId, ReimbursementClaimUpdateReqVO reqVO);

    /** 删除当前用户拥有的草稿或 AI 失败报销单。 */
    void deleteClaim(Long userId, Long id);

    /** 确认 AI 待确认报销单，并按人工修订内容重建草稿明细。 */
    void confirmClaim(Long userId, ReimbursementClaimConfirmReqVO reqVO);

    /** 提交草稿到 BPM；已存在流程实例时幂等返回原提交结果。 */
    ReimbursementClaimSubmitRespVO submitClaim(Long userId, ReimbursementClaimSubmitReqVO reqVO);

    /** 查询本人报销单；拥有 query-all 权限时可查询本租户其他用户数据。 */
    ReimbursementClaimRespVO getClaim(Long userId, Long id);

    /** 分页查询报销单，查询范围受 query-all 权限控制。 */
    PageResult<ReimbursementClaimDO> getClaimPage(Long userId, ReimbursementClaimPageReqVO reqVO);

    /** 校验报销单与附件归属后，返回 5 分钟有效的附件访问地址。 */
    String getAttachmentAccessUrl(Long userId, Long reimbursementId, Long attachmentId);

    /** 为邮件导入创建 AI 处理中的空报销单。 */
    Long createAiProcessingClaim(Long userId, Long mailboxConnectionId);

    /** 仅当报销单仍处于 AI 处理中时标记失败，避免覆盖成功回填状态。 */
    void markAiFailedIfProcessing(Long tenantId, Long reimbursementId, String failureMessage);

    /** 以报销单所属用户身份把 AI 待确认报销单自动提交到 BPM。 */
    ReimbursementClaimSubmitRespVO autoSubmitClaim(Long tenantId, Long ownerUserId, Long reimbursementId);
}
