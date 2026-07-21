package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailimport.*;
import cn.iocoder.yudao.module.reimbursement.service.mailimport.ReimbursementMailImportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * 接口控制器。
 */

@RestController
@RequestMapping("/reimbursement/mail-import")
@Validated
@RequiredArgsConstructor
public class ReimbursementMailImportController {
    private final ReimbursementMailImportService service;

    /**
     * 启动邮箱导入。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PostMapping("/start")
    @PreAuthorize("@ss.hasPermission('reimbursement:mail-import:start')")
    public CommonResult<ReimbursementMailImportStartRespVO> start(
            @Valid @RequestBody ReimbursementMailImportStartReqVO v) {
        return success(service.start(getLoginUserId(), v));
    }
}
