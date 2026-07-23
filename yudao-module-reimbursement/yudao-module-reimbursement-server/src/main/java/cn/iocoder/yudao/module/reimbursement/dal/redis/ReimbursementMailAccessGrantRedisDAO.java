package cn.iocoder.yudao.module.reimbursement.dal.redis;

import cn.hutool.crypto.SecureUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 报销邮箱短期访问授权 Redis DAO
 */
@Repository
@RequiredArgsConstructor
public class ReimbursementMailAccessGrantRedisDAO {
    private static final String KEY_PREFIX = "reimbursement:mail-access:";
    private final StringRedisTemplate stringRedisTemplate;

    public void set(String rawToken, ReimbursementMailAccessGrant grant, Duration ttl) {
        stringRedisTemplate.opsForValue().set(buildKey(rawToken), JsonUtils.toJsonString(grant), ttl);
    }

    public ReimbursementMailAccessGrant get(String rawToken) {
        String grantJson = stringRedisTemplate.opsForValue().get(buildKey(rawToken));
        return JsonUtils.parseObject(grantJson, ReimbursementMailAccessGrant.class);
    }

    public void delete(String rawToken) {
        stringRedisTemplate.delete(buildKey(rawToken));
    }

    /**
     * 构造邮箱短期访问授权的 Redis Key。
     * 
     * @param rawToken 原始访问令牌
     */
    private String buildKey(String rawToken) {
        return KEY_PREFIX + SecureUtil.sha256(rawToken);
    }

}
