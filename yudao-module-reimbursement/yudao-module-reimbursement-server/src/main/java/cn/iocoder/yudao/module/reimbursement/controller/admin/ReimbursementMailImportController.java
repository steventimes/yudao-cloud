package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.*;
import cn.iocoder.yudao.module.reimbursement.service.mailimport.ReimbursementMailImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import static cn.iocoder.yudao.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.error;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 管理后台邮件导入接口，负责创建 AI 处理中的报销单并异步启动 Dify Workflow。
 */

@RestController
@RequestMapping("/reimbursement/mail-import")
@Validated
@RequiredArgsConstructor
public class ReimbursementMailImportController {
    private final ReimbursementMailImportService service;

    @PostMapping("/start")
    @PreAuthorize("@ss.hasPermission('reimbursement:mail-import:start')")
    public CommonResult<ReimbursementMailImportStartRespVO> start(
            @Valid @RequestBody ReimbursementMailImportStartReqVO v) {
        return success(service.start(getLoginUserId(), v));
    }

    /**
     * 将本接口的请求体校验错误转换为稳定、可读的响应，避免底层方法签名暴露给前端。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<?> handleValidationException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "请求参数不正确";
        return error(BAD_REQUEST.getCode(), "请求参数不正确:" + message);
    }

    @ExceptionHandler(ServiceException.class)
    public CommonResult<?> handleServiceException(ServiceException exception) {
        return error(exception.getCode(), exception.getMessage());
    }
}
