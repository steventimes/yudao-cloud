package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.ReimbursementMailboxCreateReqVO;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementMailboxConnectionMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReimbursementMailboxServiceImplTest extends AbstractReimbursementUnitTest {

    @Test
    void createMailboxShouldRejectUnsupportedProvider() {
        ReimbursementMailboxServiceImpl service = new ReimbursementMailboxServiceImpl(
                mock(ReimbursementMailboxConnectionMapper.class),
                new ReimbursementCredentialCipher(newProperties()),
                mock(ReimbursementMailboxVerifier.class),
                newProperties());
        ReimbursementMailboxCreateReqVO request = new ReimbursementMailboxCreateReqVO();
        request.setProviderCode("UNKNOWN");
        request.setEmail("employee@example.com");
        request.setAuthorizationCode("auth-code");

        assertThrows(RuntimeException.class, () -> service.createMailbox(10L, request));
    }

    @Test
    void createMailboxShouldForceQqMailStrictImaps() {
        ReimbursementMailboxConnectionMapper mailboxConnectionMapper = mock(ReimbursementMailboxConnectionMapper.class);
        ReimbursementMailboxServiceImpl service = new ReimbursementMailboxServiceImpl(
                mailboxConnectionMapper, new ReimbursementCredentialCipher(newProperties()),
                mock(ReimbursementMailboxVerifier.class), newProperties());
        ReimbursementMailboxCreateReqVO request = new ReimbursementMailboxCreateReqVO();
        request.setProviderCode("QQ_MAIL");
        request.setEmail("UserA@QQ.COM");
        request.setUsername("");
        request.setAuthorizationCode("auth-code");

        service.createMailbox(10L, request);

        ArgumentCaptor<ReimbursementMailboxConnectionDO> mailboxCaptor = ArgumentCaptor
                .forClass(ReimbursementMailboxConnectionDO.class);
        verify(mailboxConnectionMapper).insert(mailboxCaptor.capture());
        assertEquals("imap.qq.com", mailboxCaptor.getValue().getImapHost());
        assertEquals(993, mailboxCaptor.getValue().getImapPort());
        assertEquals("strict", mailboxCaptor.getValue().getTlsVerification());
        assertEquals("UserA@QQ.COM", mailboxCaptor.getValue().getUsername());
        assertEquals("usera@qq.com", mailboxCaptor.getValue().getEmailNormalized());
    }

}
