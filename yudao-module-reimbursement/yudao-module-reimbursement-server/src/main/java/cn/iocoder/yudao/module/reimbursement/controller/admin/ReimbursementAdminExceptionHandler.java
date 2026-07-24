package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.FORBIDDEN;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;

/**
 * 报销管理端异常处理器。
 *
 * <p>限制在报销模块管理端 Controller，避免第三方全局处理器将业务异常和权限异常包装成 500。</p>
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "cn.iocoder.yudao.module.reimbursement.controller.admin")
public class ReimbursementAdminExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public CommonResult<?> handleServiceException(ServiceException exception) {
        return error(exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public CommonResult<?> handleAccessDeniedException(AccessDeniedException exception) {
        return error(FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        return validationError(exception.getBindingResult().getFieldError());
    }

    @ExceptionHandler(BindException.class)
    public CommonResult<?> handleBindException(BindException exception) {
        return validationError(exception.getBindingResult().getFieldError());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public CommonResult<?> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("请求参数不正确");
        return error(BAD_REQUEST.getCode(), "请求参数不正确: " + message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, HttpMediaTypeNotSupportedException.class})
    public CommonResult<?> handleInvalidRequestBodyException(Exception exception) {
        return error(BAD_REQUEST.getCode(), "请求体格式不正确");
    }

    private CommonResult<?> validationError(FieldError fieldError) {
        String message = fieldError == null ? "请求参数不正确" : fieldError.getDefaultMessage();
        return error(BAD_REQUEST.getCode(), "请求参数不正确: " + message);
    }

}
