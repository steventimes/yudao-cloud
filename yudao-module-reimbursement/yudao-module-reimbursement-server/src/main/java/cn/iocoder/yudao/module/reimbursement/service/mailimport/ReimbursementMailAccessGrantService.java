package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import cn.iocoder.yudao.module.reimbursement.dal.redis.ReimbursementMailAccessGrant;
import cn.iocoder.yudao.module.reimbursement.dal.redis.ReimbursementMailAccessGrantRedisDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAIL_ACCESS_OPERATION_DENIED;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_MAIL_ACCESS_TOKEN_INVALID;

/**
 * 报销邮箱短期访问授权 Service
 * 
 * @author Codex
 */
@Service
@RequiredArgsConstructor
public class ReimbursementMailAccessGrantService {

    private static final Set<String> ALLOWED_OPERATIONS = Set.of("SEARCH", "FETCH");

    private final SecureRandom secureRandom = new SecureRandom();
    private final ReimbursementMailAccessGrantRedisDAO grantRedisDAO;
    private final ReimbursementProperties reimbursementProperties;

    /**
     * 执行 issue 业务操作。
     * 
     * @param tenantId            租户编号
     * @param userId              用户编号
     * @param mailboxConnectionId 邮箱连接编号
     * @return 处理结果
     */

    public String issue(Long tenantId, Long userId, Long mailboxConnectionId) {
        String rawToken = generateRawToken();
        int ttlMinutes = reimbursementProperties.getMailbox().getAccessTokenTtlMinutes();
        ReimbursementMailAccessGrant grant = new ReimbursementMailAccessGrant();
        grant.setAccessId(UUID.randomUUID().toString());
        grant.setTenantId(tenantId);
        grant.setUserId(userId);
        grant.setMailboxConnectionId(mailboxConnectionId);
        grant.setAllowedOperations(ALLOWED_OPERATIONS);
        grant.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        grantRedisDAO.set(rawToken, grant, Duration.ofMinutes(ttlMinutes));
        return rawToken;
    }

    public ReimbursementMailAccessGrant requireGrant(String rawToken, String operation) {
        if (!ALLOWED_OPERATIONS.contains(operation)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAIL_ACCESS_OPERATION_DENIED);
        }
        ReimbursementMailAccessGrant grant = grantRedisDAO.get(rawToken);
        if (grant == null || grant.getExpiresAt() == null || grant.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAIL_ACCESS_TOKEN_INVALID);
        }
        if (grant.getAllowedOperations() == null || !grant.getAllowedOperations().contains(operation)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAIL_ACCESS_OPERATION_DENIED);
        }
        return grant;
    }

    /** 生成用于邮箱访问授权的随机令牌。 */
    private String generateRawToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}
