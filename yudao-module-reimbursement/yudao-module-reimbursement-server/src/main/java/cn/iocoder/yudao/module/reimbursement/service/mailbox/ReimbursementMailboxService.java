package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;

public interface ReimbursementMailboxService {
    Long createMailbox(Long userId, ReimbursementMailboxCreateReqVO reqVO);

    void updateMailbox(Long userId, ReimbursementMailboxUpdateReqVO reqVO);

    ReimbursementMailboxVerifyRespVO verifyMailbox(Long userId, Long id);

    ReimbursementMailboxRespVO getMailbox(Long userId, Long id);

    PageResult<ReimbursementMailboxConnectionDO> getMailboxPage(Long userId, ReimbursementMailboxPageReqVO reqVO);

    void deleteMailbox(Long userId, Long id);

    ReimbursementMailboxConnectionDO requireVerifiedOwnedMailbox(Long userId, Long id);

    ResolvedMailboxCredential resolveCredentialForInternalUse(Long connectionId);

    record ResolvedMailboxCredential(ReimbursementMailboxConnectionDO connection, String authorizationCode) {
    }
}
