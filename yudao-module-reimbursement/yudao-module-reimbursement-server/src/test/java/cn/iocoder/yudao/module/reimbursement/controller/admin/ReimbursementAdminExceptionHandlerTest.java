package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReimbursementAdminExceptionHandlerTest {

    private final ReimbursementAdminExceptionHandler handler = new ReimbursementAdminExceptionHandler();

    @Test
    void preservesBusinessError() {
        CommonResult<?> result = handler.handleServiceException(
                new ServiceException(1_070_000_001, "报销单不存在"));

        assertEquals(1_070_000_001, result.getCode());
        assertEquals("报销单不存在", result.getMsg());
    }

    @Test
    void mapsMethodSecurityFailureToForbidden() {
        CommonResult<?> result = handler.handleAccessDeniedException(
                new AccessDeniedException("denied"));

        assertEquals(FORBIDDEN.getCode(), result.getCode());
        assertEquals(FORBIDDEN.getMsg(), result.getMsg());
    }

    @Test
    void isLimitedToReimbursementAdminControllersAndWinsAdviceOrdering() {
        RestControllerAdvice advice = ReimbursementAdminExceptionHandler.class
                .getAnnotation(RestControllerAdvice.class);
        Order order = ReimbursementAdminExceptionHandler.class.getAnnotation(Order.class);

        assertTrue(String.join(",", advice.basePackages())
                .contains("cn.iocoder.yudao.module.reimbursement.controller.admin"));
        assertEquals(Ordered.HIGHEST_PRECEDENCE, order.value());
    }
}
