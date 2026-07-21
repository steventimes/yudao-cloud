package cn.iocoder.yudao.module.reimbursement.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.*;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.service.claim.ReimbursementClaimService;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "管理后台 - 智能报销")
@RestController
@RequestMapping("/reimbursement/claim")
@Validated
@RequiredArgsConstructor
public class ReimbursementClaimController {
    private final ReimbursementClaimService service;

    /**
     * 创建报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:create')")
    public CommonResult<Long> create(@Valid @RequestBody ReimbursementClaimCreateReqVO v) {
        return success(service.createClaim(getLoginUserId(), v));
    }

    /**
     * 更新报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ReimbursementClaimUpdateReqVO v) {
        service.updateClaim(getLoginUserId(), v);
        return success(true);
    }

    /**
     * 确认报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PostMapping("/confirm")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:update')")
    public CommonResult<Boolean> confirm(@Valid @RequestBody ReimbursementClaimConfirmReqVO v) {
        service.confirmClaim(getLoginUserId(), v);
        return success(true);
    }

    /**
     * 提交报销审批。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @PostMapping("/submit")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:submit')")
    public CommonResult<ReimbursementClaimSubmitRespVO> submit(@Valid @RequestBody ReimbursementClaimSubmitReqVO v) {
        return success(service.submitClaim(getLoginUserId(), v));
    }

    /**
     * 查询单条报销数据。
     * 
     * @param id 记录编号
     * @return 处理结果
     */
    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:query')")
    public CommonResult<ReimbursementClaimRespVO> get(@RequestParam Long id) {
        return success(service.getClaim(getLoginUserId(), id));
    }

    /**
     * 分页查询报销数据。
     * 
     * @param v 请求参数对象
     * @return 处理结果
     */
    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:query')")
    public CommonResult<PageResult<ReimbursementClaimDO>> page(@Valid ReimbursementClaimPageReqVO v) {
        return success(service.getClaimPage(getLoginUserId(), v));
    }

    /**
     * 执行 accessUrl 业务操作。
     * 
     * @param reimbursementId 报销单编号
     * @param attachmentId    附件编号
     * @return 处理结果
     */
    @GetMapping("/attachment/access-url")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:query')")
    public CommonResult<String> accessUrl(@RequestParam Long reimbursementId, @RequestParam Long attachmentId) {
        return success(service.getAttachmentAccessUrl(getLoginUserId(), reimbursementId, attachmentId));
    }
}
