package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class ReimbursementCredentialCipherTest extends AbstractReimbursementUnitTest {

    @Test
    void encryptShouldUseRandomIvAndDecrypt() {
        ReimbursementCredentialCipher cipher = new ReimbursementCredentialCipher(newProperties());

        String firstCiphertext = cipher.encrypt("qq-mail-auth-code");
        String secondCiphertext = cipher.encrypt("qq-mail-auth-code");

        assertNotEquals(firstCiphertext, secondCiphertext);
        assertEquals("qq-mail-auth-code", cipher.decrypt(firstCiphertext));
        assertEquals("qq-mail-auth-code", cipher.decrypt(secondCiphertext));
    }

    @Test
    void decryptShouldRejectWrongKeyAndTamperedCiphertextWithoutPlaintext() {
        ReimbursementProperties properties = newProperties();
        ReimbursementCredentialCipher cipher = new ReimbursementCredentialCipher(properties);
        String ciphertext = cipher.encrypt("secret-authorization-code");

        ReimbursementProperties wrongKeyProperties = newProperties();
        wrongKeyProperties.getMailbox().setEncryptionKey(Base64.getEncoder().encodeToString(
                "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa".getBytes(StandardCharsets.UTF_8)));
        RuntimeException wrongKeyException = assertThrows(RuntimeException.class,
                () -> new ReimbursementCredentialCipher(wrongKeyProperties).decrypt(ciphertext));
        assertFalse(String.valueOf(wrongKeyException.getMessage()).contains("secret-authorization-code"));

        RuntimeException tamperedException = assertThrows(RuntimeException.class,
                () -> cipher.decrypt(ciphertext.substring(0, ciphertext.length() - 2) + "aa"));
        assertFalse(String.valueOf(tamperedException.getMessage()).contains("secret-authorization-code"));
    }

}
