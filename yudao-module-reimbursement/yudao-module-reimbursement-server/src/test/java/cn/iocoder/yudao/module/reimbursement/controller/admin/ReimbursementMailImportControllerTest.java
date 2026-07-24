package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ReimbursementMailImportControllerTest {

    @Test
    void handleValidationExceptionReturnsSafeMessage() {
        ReimbursementAdminExceptionHandler handler = new ReimbursementAdminExceptionHandler();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "mailboxConnectionId", "请选择邮箱连接"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        CommonResult<?> result = handler.handleMethodArgumentNotValidException(exception);

        assertEquals(BAD_REQUEST.getCode(), result.getCode());
        assertEquals("请求参数不正确: 请选择邮箱连接", result.getMsg());
        assertFalse(result.getMsg().contains("cn.iocoder"));
    }

    @Test
    void handleServiceExceptionReturnsBusinessError() {
        ReimbursementAdminExceptionHandler handler = new ReimbursementAdminExceptionHandler();
        CommonResult<?> result = handler.handleServiceException(
                new ServiceException(1_070_000_021, "邮件时间筛选条件不合法"));

        assertEquals(1_070_000_021, result.getCode());
        assertEquals("邮件时间筛选条件不合法", result.getMsg());
    }
}
