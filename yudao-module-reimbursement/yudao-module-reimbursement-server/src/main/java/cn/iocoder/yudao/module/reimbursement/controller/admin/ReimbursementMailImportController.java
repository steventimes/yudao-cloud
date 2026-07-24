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

}
