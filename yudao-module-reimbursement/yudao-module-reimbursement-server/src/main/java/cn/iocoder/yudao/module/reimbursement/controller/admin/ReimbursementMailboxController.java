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

/**
 * 接口控制器。
 */

@RestController
@RequestMapping("/reimbursement/mailbox")
@Validated
@RequiredArgsConstructor
public class ReimbursementMailboxController {
    private final ReimbursementMailboxService service;

    /**
     * 创建报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<Long> create(@Valid @RequestBody ReimbursementMailboxCreateReqVO v) {
        return success(service.createMailbox(getLoginUserId(), v));
    }

    /**
     * 更新报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<Boolean> update(@Valid @RequestBody ReimbursementMailboxUpdateReqVO v) {
        service.updateMailbox(getLoginUserId(), v);
        return success(true);
    }

    /**
     * 查询单条报销数据。
     * 
     * @param id 记录编号
     * @return 处理结果
     */
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<ReimbursementMailboxRespVO> get(@RequestParam Long id) {
        return success(service.getMailbox(getLoginUserId(), id));
    }

    /**
     * 分页查询报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<PageResult<ReimbursementMailboxRespVO>> page(@Valid ReimbursementMailboxPageReqVO v) {
        return success(service.getMailboxPage(getLoginUserId(), v));
    }

    /**
     * 验证邮箱配置。
     * 
     * @param id 记录编号
     * @return 处理结果
     */
    @PostMapping("/verify")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<ReimbursementMailboxVerifyRespVO> verify(@RequestParam Long id) {
        return success(service.verifyMailbox(getLoginUserId(), id));
    }

    /**
     * 删除报销数据。
     * 
     * @param id 记录编号
     * @return 处理结果
     */
    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('reimbursement:mailbox:manage')")
    public CommonResult<Boolean> delete(@RequestParam Long id) {
        service.deleteMailbox(getLoginUserId(), id);
        return success(true);
    }
}
