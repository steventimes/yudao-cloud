package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import jakarta.mail.Folder;
import jakarta.mail.Session;
import jakarta.mail.Store;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.Set;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAILBOX_CONFIG_INVALID;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAILBOX_CREDENTIAL_INVALID;

/**
 * Jakarta Mail 邮箱连接验证器
 * 
 * @author Codex
 */
@Component
public class JakartaMailReimbursementMailboxVerifier implements ReimbursementMailboxVerifier {

    private static final Set<String> INSECURE_DEV_ALLOWED_HOSTS = Set.of("greenmail", "localhost", "127.0.0.1", "::1");

    /**
     * 验证邮箱配置。
     * 
     * @param host              邮箱服务器地址
     * @param port              邮箱服务器端口
     * @param username          邮箱用户名
     * @param authorizationCode 邮箱授权码
     * @param tlsVerification   TLS 校验模式
     * @return 处理结果
     */

    @Override
    public void verify(String host, int port, String username, String authorizationCode, String tlsVerification) {
        validateTlsVerification(host, tlsVerification);
        Store store = null;
        Folder inbox = null;
        try {
            Properties properties = buildMailProperties(tlsVerification);
            Session session = Session.getInstance(properties);
            store = session.getStore("imaps");
            store.connect(host, port, username, authorizationCode);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CREDENTIAL_INVALID);
        } finally {
            closeQuietly(inbox);
            closeQuietly(store);
        }
    }

    /**
     * 校验TlsVerification参数。
     * 
     * @param host            邮箱服务器地址
     * @param tlsVerification TLS 校验模式
     */
    private void validateTlsVerification(String host, String tlsVerification) {
        if ("strict".equals(tlsVerification)) {
            return;
        }
        if (!"insecure-dev".equals(tlsVerification) || !INSECURE_DEV_ALLOWED_HOSTS.contains(host)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
    }

    /**
     * 构建MailProperties结果。
     * 
     * @param tlsVerification TLS 校验模式
     */
    private Properties buildMailProperties(String tlsVerification) {
        Properties properties = new Properties();
        properties.put("mail.imaps.connectiontimeout", "15000");
        properties.put("mail.imaps.timeout", "15000");
        properties.put("mail.imaps.ssl.enable", "true");
        if ("insecure-dev".equals(tlsVerification)) {
            properties.put("mail.imaps.ssl.trust", "*");
            properties.put("mail.imaps.ssl.checkserveridentity", "false");
        } else {
            properties.put("mail.imaps.ssl.checkserveridentity", "true");
        }
        return properties;
    }

    /**
     * 处理closeQuietly逻辑。
     * 
     * @param folder 方法调用所需的folder数据
     */
    private void closeQuietly(Folder folder) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(false);
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 处理closeQuietly逻辑。
     * 
     * @param store 方法调用所需的store数据
     */
    private void closeQuietly(Store store) {
        try {
            if (store != null && store.isConnected()) {
                store.close();
            }
        } catch (Exception ignored) {
        }
    }

}
