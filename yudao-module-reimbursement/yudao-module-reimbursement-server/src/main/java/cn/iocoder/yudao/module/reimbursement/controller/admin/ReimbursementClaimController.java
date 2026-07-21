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

@Tag(name = "管理后台 - 智能报销")
@RestController
@RequestMapping("/reimbursement/claim")
@Validated
@RequiredArgsConstructor
public class ReimbursementClaimController {
    private final ReimbursementClaimService service;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:create')")
    public CommonResult<Long> create(@Valid @RequestBody ReimbursementClaimCreateReqVO v) {
        return success(service.createClaim(getLoginUserId(), v));
    }

    @PutMapping("/update")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody ReimbursementClaimUpdateReqVO v) {
        service.updateClaim(getLoginUserId(), v);
        return success(true);
    }

    @PostMapping("/confirm")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:update')")
    public CommonResult<Boolean> confirm(@Valid @RequestBody ReimbursementClaimConfirmReqVO v) {
        service.confirmClaim(getLoginUserId(), v);
        return success(true);
    }

    @PostMapping("/submit")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:submit')")
    public CommonResult<ReimbursementClaimSubmitRespVO> submit(@Valid @RequestBody ReimbursementClaimSubmitReqVO v) {
        return success(service.submitClaim(getLoginUserId(), v));
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:query')")
    public CommonResult<ReimbursementClaimRespVO> get(@RequestParam Long id) {
        return success(service.getClaim(getLoginUserId(), id));
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:query')")
    public CommonResult<PageResult<ReimbursementClaimDO>> page(@Valid ReimbursementClaimPageReqVO v) {
        return success(service.getClaimPage(getLoginUserId(), v));
    }

    @GetMapping("/attachment/access-url")
    @PreAuthorize("@ss.hasPermission('reimbursement:claim:query')")
    public CommonResult<String> accessUrl(@RequestParam Long reimbursementId, @RequestParam Long attachmentId) {
        return success(service.getAttachmentAccessUrl(getLoginUserId(), reimbursementId, attachmentId));
    }
}
