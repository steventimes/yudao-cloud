package cn.iocoder.yudao.module.reimbursement.service.support;

import cn.iocoder.yudao.module.reimbursement.AbstractReimbursementUnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReimbursementInternalAuthServiceTest extends AbstractReimbursementUnitTest {

    @Test
    void requireAuthorizedShouldAcceptBearerToken() {
        ReimbursementInternalAuthService authService = new ReimbursementInternalAuthService(newProperties());

        assertDoesNotThrow(() -> authService.requireAuthorized("Bearer internal-token"));
    }

    @Test
    void requireAuthorizedShouldRejectMissingOrWrongToken() {
        ReimbursementInternalAuthService authService = new ReimbursementInternalAuthService(newProperties());

        assertThrows(RuntimeException.class, () -> authService.requireAuthorized(null));
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.requireAuthorized("Bearer wrong-token"));
        assert !String.valueOf(exception.getMessage()).contains("wrong-token");
    }

}
