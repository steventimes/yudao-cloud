package cn.iocoder.yudao.module.reimbursement.config;

import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementAiSubmitModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yudao.reimbursement")
public class ReimbursementProperties {
    private final Ai ai = new Ai();
    private final Dify dify = new Dify();
    private final Mailbox mailbox = new Mailbox();
    private final Internal internal = new Internal();

    @Data
    public static class Ai {
        private ReimbursementAiSubmitModeEnum submitMode = ReimbursementAiSubmitModeEnum.DRAFT_ONLY;
    }

    @Data
    public static class Dify {
        private boolean enabled;
        private String apiBaseUrl;
        private String apiKey;
        private int timeoutMillis = 300000;
    }

    @Data
    public static class Mailbox {
        private boolean allowCustomProvider;
        private String encryptionKey;
        private int accessTokenTtlMinutes = 15;
    }

    @Data
    public static class Internal {
        private String serviceToken;
    }
}
