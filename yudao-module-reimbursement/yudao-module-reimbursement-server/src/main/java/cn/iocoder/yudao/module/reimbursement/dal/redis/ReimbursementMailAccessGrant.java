package cn.iocoder.yudao.module.reimbursement.dal.redis;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ReimbursementMailAccessGrant {
    private String accessId;
    private Long tenantId;
    private Long userId;
    private Long mailboxConnectionId;
    private Set<String> allowedOperations;
    private LocalDateTime expiresAt;
}
