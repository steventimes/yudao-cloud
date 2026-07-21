package cn.iocoder.yudao.module.reimbursement.service.mailbox;

public interface ReimbursementMailboxVerifier {
    void verify(String host, int port, String username, String authorizationCode, String tlsVerification);
}
