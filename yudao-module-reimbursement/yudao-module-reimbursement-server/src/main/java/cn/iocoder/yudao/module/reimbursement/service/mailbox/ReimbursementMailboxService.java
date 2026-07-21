package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;

/**
 * ReimbursementMailboxService，业务服务。
 */
public interface ReimbursementMailboxService {
    /**
     * 执行 createMailbox 业务操作。
     * 
     * @param userId 用户编号
     * @param reqVO  请求参数对象
     */
    Long createMailbox(Long userId, ReimbursementMailboxCreateReqVO reqVO);

    void updateMailbox(Long userId, ReimbursementMailboxUpdateReqVO reqVO);

    /**
     * 执行 verifyMailbox 业务操作。
     * 
     * @param userId 用户编号
     * @param id     记录编号
     */
    ReimbursementMailboxVerifyRespVO verifyMailbox(Long userId, Long id);

    /**
     * 执行 getMailbox 业务操作。
     * 
     * @param userId 用户编号
     * @param id     记录编号
     */
    ReimbursementMailboxRespVO getMailbox(Long userId, Long id);

    /**
     * 执行 getMailboxPage 业务操作。
     * 
     * @param userId 用户编号
     * @param reqVO  请求参数对象
     */
    PageResult<ReimbursementMailboxRespVO> getMailboxPage(Long userId, ReimbursementMailboxPageReqVO reqVO);

    void deleteMailbox(Long userId, Long id);

    /**
     * 执行 requireVerifiedOwnedMailbox 业务操作。
     * 
     * @param userId 用户编号
     * @param id     记录编号
     */
    ReimbursementMailboxConnectionDO requireVerifiedOwnedMailbox(Long userId, Long id);

    /**
     * 执行 resolveCredentialForInternalUse 业务操作。
     * 
     * @param connectionId 邮箱连接编号
     */
    ResolvedMailboxCredential resolveCredentialForInternalUse(Long connectionId);

    /**
     * 已解密的邮箱凭据，仅供内部访问。
     * 
     * @param connection        邮箱连接配置
     * @param authorizationCode 解密后的邮箱授权码
     */
    record ResolvedMailboxCredential(ReimbursementMailboxConnectionDO connection, String authorizationCode) {
    }
}
