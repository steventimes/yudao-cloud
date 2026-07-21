package cn.iocoder.yudao.module.reimbursement.service.mailimport;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import cn.iocoder.yudao.module.reimbursement.dal.redis.ReimbursementMailAccessGrant;
import cn.iocoder.yudao.module.reimbursement.dal.redis.ReimbursementMailAccessGrantRedisDAO;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReimbursementMailAccessGrantServiceTest extends AbstractReimbursementUnitTest {

    @Test
    void issueShouldStoreGrantUnderDaoAndAllowSearchFetchOnly() {
        ReimbursementMailAccessGrantRedisDAO grantRedisDAO = mock(ReimbursementMailAccessGrantRedisDAO.class);
        ReimbursementMailAccessGrantService grantService = new ReimbursementMailAccessGrantService(grantRedisDAO,
                newProperties());

        String rawToken = grantService.issue(1L, 10L, 100L);

        assertNotNull(rawToken);
        ArgumentCaptor<ReimbursementMailAccessGrant> grantCaptor = ArgumentCaptor
                .forClass(ReimbursementMailAccessGrant.class);
        verify(grantRedisDAO).set(eq(rawToken), grantCaptor.capture(), eq(Duration.ofMinutes(15)));
        assertEquals(1L, grantCaptor.getValue().getTenantId());
        assertEquals(10L, grantCaptor.getValue().getUserId());
        assertEquals(100L, grantCaptor.getValue().getMailboxConnectionId());
        assertEquals(Set.of("SEARCH", "FETCH"), grantCaptor.getValue().getAllowedOperations());
    }

    @Test
    void requireGrantShouldRejectExpiredAndUnsupportedOperations() {
        ReimbursementMailAccessGrantRedisDAO grantRedisDAO = mock(ReimbursementMailAccessGrantRedisDAO.class);
        ReimbursementMailAccessGrant expiredGrant = new ReimbursementMailAccessGrant();
        expiredGrant.setAllowedOperations(Set.of("SEARCH", "FETCH"));
        expiredGrant.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(grantRedisDAO.get("raw-token")).thenReturn(expiredGrant);
        ReimbursementMailAccessGrantService grantService = new ReimbursementMailAccessGrantService(grantRedisDAO,
                newProperties());

        assertThrows(RuntimeException.class, () -> grantService.requireGrant("raw-token", "DELETE"));
        assertThrows(RuntimeException.class, () -> grantService.requireGrant("raw-token", "SEARCH"));
    }

}
