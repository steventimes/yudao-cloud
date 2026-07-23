package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;

/**
 * 报销邮箱连接服务，负责凭据加密、归属校验和连接验证。
 */
public interface ReimbursementMailboxService {
    /** 创建邮箱连接并加密保存授权码；新连接需验证后才能导入。 */
    Long createMailbox(Long userId, ReimbursementMailboxCreateReqVO reqVO);

    /** 更新本人邮箱连接；配置变化后需重新验证。 */
    void updateMailbox(Long userId, ReimbursementMailboxUpdateReqVO reqVO);

    /** 使用解密后的授权码验证 IMAPS 连接并更新验证状态。 */
    ReimbursementMailboxVerifyRespVO verifyMailbox(Long userId, Long id);

    /** 查询本人邮箱连接，响应不包含授权码。 */
    ReimbursementMailboxRespVO getMailbox(Long userId, Long id);

    /** 分页查询本人邮箱连接，响应不包含授权码。 */
    PageResult<ReimbursementMailboxRespVO> getMailboxPage(Long userId, ReimbursementMailboxPageReqVO reqVO);

    /** 物理删除本人邮箱连接及加密凭据。 */
    void deleteMailbox(Long userId, Long id);

    /** 查询并校验当前用户拥有且已验证的邮箱连接。 */
    ReimbursementMailboxConnectionDO requireVerifiedOwnedMailbox(Long userId, Long id);

    /** 在当前租户上下文中解析邮箱连接及解密授权码，仅供内部短期授权链路使用。 */
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
