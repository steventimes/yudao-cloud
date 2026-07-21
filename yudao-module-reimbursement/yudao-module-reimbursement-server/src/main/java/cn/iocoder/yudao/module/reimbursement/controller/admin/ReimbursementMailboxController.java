package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.*;
import cn.iocoder.yudao.module.reimbursement.service.mailbox.ReimbursementMailboxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@RestController
@RequestMapping("/reimbursement/mailbox")
@Validated
@RequiredArgsConstructor
public class ReimbursementMailboxController {
    private final ReimbursementMailboxService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<Long> create(@Valid @RequestBody ReimbursementMailboxCreateReqVO v) {
        return success(service.createMailbox(getLoginUserId(), v));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<Boolean> update(@Valid @RequestBody ReimbursementMailboxUpdateReqVO v) {
        service.updateMailbox(getLoginUserId(), v);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<ReimbursementMailboxRespVO> get(@RequestParam Long id) {
        return success(service.getMailbox(getLoginUserId(), id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<PageResult<ReimbursementMailboxRespVO>> page(@Valid ReimbursementMailboxPageReqVO v) {
        return success(service.getMailboxPage(getLoginUserId(), v));
    }

    @PostMapping("/verify")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<ReimbursementMailboxVerifyRespVO> verify(@RequestParam Long id) {
        return success(service.verifyMailbox(getLoginUserId(), id));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<Boolean> delete(@RequestParam Long id) {
        service.deleteMailbox(getLoginUserId(), id);
        return success(true);
    }
}
