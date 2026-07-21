package cn.iocoder.yudao.module.reimbursement.dal.redis;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * ReimbursementMailAccessGrant，Redis 数据访问对象。
 */

@Data
public class ReimbursementMailAccessGrant {
    private String accessId;
    /** 租户编号。 */
    private Long tenantId;
    /** 用户编号。 */
    private Long userId;
    /** 邮箱连接编号。 */
    private Long mailboxConnectionId;
    private Set<String> allowedOperations;
    /** 过期时间。 */
    private LocalDateTime expiresAt;
}
