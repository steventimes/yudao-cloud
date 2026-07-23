package cn.iocoder.yudao.module.reimbursement.service.mailbox;

/**
 * 报销邮箱连接验证器。
 */
public interface ReimbursementMailboxVerifier {
    void verify(String host, int port, String username, String authorizationCode, String tlsVerification);
}
