package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAILBOX_CONFIG_INVALID;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAILBOX_CREDENTIAL_INVALID;

/**
 * 报销邮箱授权码加解密器
 * 
 * @author Codex
 */
@Component
@RequiredArgsConstructor
public class ReimbursementCredentialCipher {

    private static final String CIPHER_ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH_BYTES = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final String VERSION_PREFIX = "v1";

    private final SecureRandom secureRandom = new SecureRandom();
    private final ReimbursementProperties reimbursementProperties;

    /**
     * 加密敏感数据。
     * 
     * @param plaintext 待加密的明文授权码
     * @return 处理结果
     */

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, buildSecretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
            return VERSION_PREFIX + ':' + encoder.encodeToString(iv) + ':' + encoder.encodeToString(ciphertext);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
    }

    public String decrypt(String encodedCiphertext) {
        try {
            String[] parts = encodedCiphertext.split(":", 3);
            if (parts.length != 3 || !VERSION_PREFIX.equals(parts[0])) {
                throw new IllegalArgumentException("invalid ciphertext");
            }
            Base64.Decoder decoder = Base64.getUrlDecoder();
            byte[] iv = decoder.decode(parts[1]);
            byte[] ciphertext = decoder.decode(parts[2]);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, buildSecretKey(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CREDENTIAL_INVALID);
        }
    }

    /** 构建用于 AES 加解密的密钥。 */
    private SecretKeySpec buildSecretKey() {
        String configuredKey = reimbursementProperties.getMailbox().getEncryptionKey();
        try {
            byte[] keyBytes = Base64.getDecoder().decode(configuredKey == null ? "" : configuredKey);
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("invalid key length");
            }
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
    }

}
