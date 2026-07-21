package cn.iocoder.yudao.module.reimbursement.service.mailbox;

/**
 * ReimbursementMailboxVerifier，业务服务。
 */
public interface ReimbursementMailboxVerifier {
    void verify(String host, int port, String username, String authorizationCode, String tlsVerification);
}
