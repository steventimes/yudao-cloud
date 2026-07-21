package cn.iocoder.yudao.module.reimbursement;

import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public abstract class AbstractReimbursementUnitTest {

    public ReimbursementProperties newProperties() {
        ReimbursementProperties properties = new ReimbursementProperties();
        properties.getMailbox().setEncryptionKey(Base64.getEncoder().encodeToString(
                "12345678901234567890123456789012".getBytes(StandardCharsets.UTF_8)));
        properties.getMailbox().setAccessTokenTtlMinutes(15);
        properties.getInternal().setServiceToken("internal-token");
        properties.getDify().setEnabled(true);
        properties.getDify().setApiBaseUrl("https://dify.example.com/v1");
        properties.getDify().setApiKey("dify-api-key");
        properties.getDify().setTimeoutMillis(300000);
        return properties;
    }

}
