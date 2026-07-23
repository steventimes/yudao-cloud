package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

class JakartaMailReimbursementMailboxVerifierTest {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.IMAPS.dynamicPort());

    @Test
    void verifyShouldAllowGreenMailWithInsecureDevTls() {
        greenMail.setUser("employee@example.com", "employee@example.com", "auth-code");

        new JakartaMailReimbursementMailboxVerifier().verify(
                "localhost", greenMail.getImaps().getPort(), "employee@example.com", "auth-code", "insecure-dev");
    }

    @Test
    void verifyShouldRejectRemoteHostForInsecureDevTls() {
        JakartaMailReimbursementMailboxVerifier verifier = new JakartaMailReimbursementMailboxVerifier();

        assertThrows(RuntimeException.class,
                () -> verifier.verify("mail.qq.com", 993, "employee@example.com", "auth-code", "insecure-dev"));
    }

    @Test
    void verifyShouldRejectWrongPasswordWithoutLeakingPassword() {
        greenMail.setUser("user-a@localhost", "user-a@localhost", "password-a");
        JakartaMailReimbursementMailboxVerifier verifier = new JakartaMailReimbursementMailboxVerifier();

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> verifier.verify("localhost", greenMail.getImaps().getPort(),
                        "user-a@localhost", "wrong-password", "insecure-dev"));
        org.junit.jupiter.api.Assertions.assertFalse(
                String.valueOf(exception.getMessage()).contains("wrong-password"));
    }

}
