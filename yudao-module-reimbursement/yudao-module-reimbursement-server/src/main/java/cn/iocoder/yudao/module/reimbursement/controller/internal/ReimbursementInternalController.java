package cn.iocoder.yudao.module.reimbursement.controller.internal;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.reimbursement.controller.internal.vo.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;
import cn.iocoder.yudao.module.reimbursement.dal.redis.ReimbursementMailAccessGrant;
import cn.iocoder.yudao.module.reimbursement.enums.ApiConstants;
import cn.iocoder.yudao.module.reimbursement.service.ai.ReimbursementAiService;
import cn.iocoder.yudao.module.reimbursement.service.mailbox.ReimbursementMailboxService;
import cn.iocoder.yudao.module.reimbursement.service.mailimport.ReimbursementMailAccessGrantService;
import cn.iocoder.yudao.module.reimbursement.service.support.ReimbursementInternalAuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.*;

/**
 * 报销内部 RPC Controller
 * 
 * @author Codex
 */
@RestController
@RequestMapping(ApiConstants.PREFIX)
@Validated
@TenantIgnore
@PermitAll
@RequiredArgsConstructor
public class ReimbursementInternalController {

    private final ReimbursementInternalAuthService internalAuthService;
    private final ReimbursementMailAccessGrantService grantService;
    private final ReimbursementMailboxService mailboxService;
    private final ReimbursementAiService aiService;

    /**
     * 解析邮箱访问授权。
     * 
     * @param authorizationHeader 内部服务认证请求头
     * @param reqVO               请求参数对象
     * @return 处理结果
     */

    @PostMapping("/mail-access/resolve")
    public CommonResult<ReimbursementMailAccessResolveRespVO> resolveMailboxAccess(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ReimbursementMailAccessResolveReqVO reqVO) {
        internalAuthService.requireAuthorized(authorizationHeader);
        ReimbursementMailAccessGrant grant = grantService.requireGrant(
                reqVO.getMailAccessToken(), reqVO.getOperation());
        return success(TenantUtils.execute(grant.getTenantId(), () -> buildMailboxAccessRespVO(grant)));
    }

    /**
     * 上传报销附件。
     * 
     * @param authorizationHeader 内部服务认证请求头
     * @param tenantId            租户编号
     * @param reimbursementId     报销单编号
     * @param externalArtifactId  外部附件产物编号
     * @param sha256              文件 SHA-256 摘要
     * @param documentType        单据类型
     * @param file                上传的附件文件
     * @return 处理结果
     */

    @PostMapping("/ai-artifact/upload")
    public CommonResult<ReimbursementAiArtifactUploadRespVO> uploadAiArtifact(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam Long tenantId,
            @RequestParam Long reimbursementId,
            @RequestParam String externalArtifactId,
            @RequestParam String sha256,
            @RequestParam String documentType,
            @RequestPart MultipartFile file) {
        internalAuthService.requireAuthorized(authorizationHeader);
        return success(TenantUtils.execute(tenantId, () -> aiService.uploadAiArtifact(
                tenantId, reimbursementId, externalArtifactId, sha256, documentType, file)));
    }

    /**
     * 应用 AI 识别结果。
     * 
     * @param authorizationHeader 内部服务认证请求头
     * @param reqVO               请求参数对象
     * @return 处理结果
     */

    @PostMapping("/ai-fill")
    public CommonResult<ReimbursementAiFillRespVO> applyAiFill(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ReimbursementAiFillReqVO reqVO) {
        internalAuthService.requireAuthorized(authorizationHeader);
        return success(
                TenantUtils.execute(reqVO.getTenantId(), () -> aiService.applyAiFill(reqVO.getTenantId(), reqVO)));
    }

    /**
     * 构建MailboxAccessRespVO结果。
     * 
     * @param grant 邮箱访问授权记录
     */
    private ReimbursementMailAccessResolveRespVO buildMailboxAccessRespVO(ReimbursementMailAccessGrant grant) {
        ReimbursementMailboxService.ResolvedMailboxCredential credential = mailboxService
                .resolveCredentialForInternalUse(
                        grant.getMailboxConnectionId());
        ReimbursementMailboxConnectionDO mailboxConnection = credential.connection();
        if (!Objects.equals(mailboxConnection.getOwnerUserId(), grant.getUserId())) {
            throw exception(REIMBURSEMENT_MAIL_ACCESS_TOKEN_INVALID);
        }
        if (!Objects.equals(mailboxConnection.getStatus(), 1)) {
            throw exception(REIMBURSEMENT_MAILBOX_NOT_VERIFIED);
        }

        ReimbursementMailAccessResolveRespVO respVO = new ReimbursementMailAccessResolveRespVO();
        respVO.setExecutionId(grant.getAccessId());
        respVO.setConnectionId(String.valueOf(mailboxConnection.getId()));
        respVO.setTenantId(String.valueOf(grant.getTenantId()));
        respVO.setProviderCode(mailboxConnection.getProviderCode());
        respVO.setImapHost(mailboxConnection.getImapHost());
        respVO.setImapPort(mailboxConnection.getImapPort());
        respVO.setUsername(mailboxConnection.getUsername());
        respVO.setAuthorizationCode(credential.authorizationCode());
        respVO.setCredentialVersion("1");
        respVO.setTlsVerification(mailboxConnection.getTlsVerification());
        respVO.setExpiresTime(grant.getExpiresAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return respVO;
    }

}
