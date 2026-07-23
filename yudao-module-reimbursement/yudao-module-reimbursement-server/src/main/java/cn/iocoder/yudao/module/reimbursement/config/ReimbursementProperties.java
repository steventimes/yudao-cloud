package cn.iocoder.yudao.module.reimbursement.config;

import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementAiSubmitModeEnum;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 智能报销模块配置。
 */

@Data
@ConfigurationProperties(prefix = "yudao.reimbursement")
public class ReimbursementProperties {
    /** AI 配置。 */
    private final Ai ai = new Ai();
    /** Dify 配置。 */
    private final Dify dify = new Dify();
    /** 邮箱配置。 */
    private final Mailbox mailbox = new Mailbox();
    /** 内部服务配置。 */
    private final Internal internal = new Internal();

    /** AI 提交配置。 */
    @Data
    public static class Ai {
        /** 提交模式。 */
        private ReimbursementAiSubmitModeEnum submitMode = ReimbursementAiSubmitModeEnum.DRAFT_ONLY;
    }

    /** Dify 工作流配置。 */
    @Data
    public static class Dify {
        /** 是否启用。 */
        private boolean enabled;
        /** 接口基础地址。 */
        private String apiBaseUrl;
        /** 接口密钥。 */
        private String apiKey;
        /** 请求超时时间（毫秒）。 */
        private int timeoutMillis = 300000;
    }

    /** 邮箱配置。 */
    @Data
    public static class Mailbox {
        /** 是否允许自定义邮箱服务商。 */
        private boolean allowCustomProvider;
        /** 邮箱授权码加密密钥。 */
        private String encryptionKey;
        /** 访问令牌有效期（分钟）。 */
        private int accessTokenTtlMinutes = 15;
    }

    /** 内部服务配置。 */
    @Data
    public static class Internal {
        /** 内部服务令牌。 */
        private String serviceToken;
    }
}
