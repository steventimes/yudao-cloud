package cn.iocoder.yudao.module.reimbursement.service.mailbox;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;
import cn.iocoder.yudao.module.reimbursement.dal.mysql.ReimbursementMailboxConnectionMapper;
import cn.iocoder.yudao.module.reimbursement.enums.ReimbursementMailboxProviderEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;

import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.*;

/**
 * 报销邮箱连接服务实现。
 */
@Service
@RequiredArgsConstructor
public class ReimbursementMailboxServiceImpl implements ReimbursementMailboxService {

    private final ReimbursementMailboxConnectionMapper mailboxConnectionMapper;
    private final ReimbursementCredentialCipher credentialCipher;
    private final ReimbursementMailboxVerifier mailboxVerifier;
    private final ReimbursementProperties reimbursementProperties;

    @Override
    @Transactional
    public Long createMailbox(Long userId, ReimbursementMailboxCreateReqVO createReqVO) {
        ReimbursementMailboxConnectionDO mailboxConnection = new ReimbursementMailboxConnectionDO();
        applyMailboxConfig(mailboxConnection, createReqVO.getProviderCode(), createReqVO.getEmail(),
                createReqVO.getUsername(), createReqVO.getImapHost(), createReqVO.getImapPort(),
                createReqVO.getTlsVerification());
        mailboxConnection.setOwnerUserId(userId);
        mailboxConnection.setCredentialCiphertext(credentialCipher.encrypt(createReqVO.getAuthorizationCode()));
        mailboxConnection.setStatus(0);
        mailboxConnectionMapper.insert(mailboxConnection);
        return mailboxConnection.getId();
    }

    @Override
    @Transactional
    public void updateMailbox(Long userId, ReimbursementMailboxUpdateReqVO updateReqVO) {
        ReimbursementMailboxConnectionDO mailboxConnection = requireOwnedMailbox(userId, updateReqVO.getId());
        applyMailboxConfig(mailboxConnection, updateReqVO.getProviderCode(), updateReqVO.getEmail(),
                updateReqVO.getUsername(), updateReqVO.getImapHost(), updateReqVO.getImapPort(),
                updateReqVO.getTlsVerification());
        if (StrUtil.isNotBlank(updateReqVO.getAuthorizationCode())) {
            mailboxConnection.setCredentialCiphertext(credentialCipher.encrypt(updateReqVO.getAuthorizationCode()));
        }
        mailboxConnection.setStatus(0);
        mailboxConnectionMapper.updateById(mailboxConnection);
    }

    @Override
    @Transactional
    public ReimbursementMailboxVerifyRespVO verifyMailbox(Long userId, Long id) {
        ReimbursementMailboxConnectionDO mailboxConnection = requireOwnedMailbox(userId, id);
        String authorizationCode = credentialCipher.decrypt(mailboxConnection.getCredentialCiphertext());
        mailboxVerifier.verify(mailboxConnection.getImapHost(), mailboxConnection.getImapPort(),
                mailboxConnection.getUsername(), authorizationCode, mailboxConnection.getTlsVerification());

        mailboxConnection.setStatus(1);
        mailboxConnection.setVerifiedAt(LocalDateTime.now());
        mailboxConnection.setLastFailureMessage(null);
        mailboxConnectionMapper.updateById(mailboxConnection);

        ReimbursementMailboxVerifyRespVO verifyRespVO = new ReimbursementMailboxVerifyRespVO();
        verifyRespVO.setId(id);
        verifyRespVO.setStatus(1);
        verifyRespVO.setMessage("验证成功");
        return verifyRespVO;
    }


    @Override
    public ReimbursementMailboxRespVO getMailbox(Long userId, Long id) {
        return buildMailboxRespVO(requireOwnedMailbox(userId, id));
    }


    @Override
    public PageResult<ReimbursementMailboxRespVO> getMailboxPage(Long userId,
            ReimbursementMailboxPageReqVO pageReqVO) {
        PageResult<ReimbursementMailboxConnectionDO> page = mailboxConnectionMapper.selectPage(userId, pageReqVO);
        return new PageResult<>(CollectionUtils.convertList(page.getList(), this::buildMailboxRespVO), page.getTotal());
    }


    @Override
    public void deleteMailbox(Long userId, Long id) {
        requireOwnedMailbox(userId, id);
        int deleted = mailboxConnectionMapper.deletePermanently(id,
                TenantContextHolder.getRequiredTenantId(), userId);
        if (deleted != 1) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_NOT_EXISTS);
        }
    }


    @Override
    public ReimbursementMailboxConnectionDO requireVerifiedOwnedMailbox(Long userId, Long id) {
        ReimbursementMailboxConnectionDO mailboxConnection = requireOwnedMailbox(userId, id);
        if (!Objects.equals(mailboxConnection.getStatus(), 1)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_NOT_VERIFIED);
        }
        return mailboxConnection;
    }


    @Override
    public ResolvedMailboxCredential resolveCredentialForInternalUse(Long connectionId) {
        ReimbursementMailboxConnectionDO mailboxConnection = mailboxConnectionMapper.selectById(connectionId);
        if (mailboxConnection == null) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_NOT_EXISTS);
        }
        return new ResolvedMailboxCredential(mailboxConnection,
                credentialCipher.decrypt(mailboxConnection.getCredentialCiphertext()));
    }

    private void applyMailboxConfig(ReimbursementMailboxConnectionDO mailboxConnection, String providerCode,
            String email, String username, String imapHost, Integer imapPort,
            String tlsVerification) {
        ReimbursementMailboxProviderEnum provider;
        try {
            provider = ReimbursementMailboxProviderEnum.valueOf(StrUtil.trim(providerCode));
        } catch (Exception ex) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
        mailboxConnection.setProviderCode(provider.name());
        String normalizedEmail = StrUtil.trim(email);
        if (StrUtil.isBlank(normalizedEmail)) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
        mailboxConnection.setEmailNormalized(normalizedEmail.toLowerCase(Locale.ROOT));
        if (provider == ReimbursementMailboxProviderEnum.QQ_MAIL) {
            mailboxConnection.setImapHost("imap.qq.com");
            mailboxConnection.setImapPort(993);
            mailboxConnection.setTlsVerification("strict");
            mailboxConnection.setUsername(StrUtil.blankToDefault(username, email));
            return;
        }
        if (!reimbursementProperties.getMailbox().isAllowCustomProvider()) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CUSTOM_DISABLED);
        }
        if (StrUtil.isBlank(imapHost) || imapHost.chars().anyMatch(Character::isWhitespace)
                || imapHost.startsWith("http://") || imapHost.startsWith("https://")
                || imapHost.contains("/") || imapPort == null || imapPort < 1 || imapPort > 65535
                || StrUtil.isBlank(username) || !CollectionUtils.containsAny(tlsVerification, "strict", "insecure-dev")) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
        if ("insecure-dev".equals(tlsVerification)
                && !CollectionUtils.containsAny(imapHost, "greenmail", "localhost", "127.0.0.1", "::1")) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_CONFIG_INVALID);
        }
        mailboxConnection.setImapHost(imapHost);
        mailboxConnection.setImapPort(imapPort);
        mailboxConnection.setTlsVerification(tlsVerification);
        mailboxConnection.setUsername(username);
    }

    /**
     * 查询当前用户拥有的邮箱连接。
     * 
     * @param userId 用户编号
     * @param id     记录编号
     */
    private ReimbursementMailboxConnectionDO requireOwnedMailbox(Long userId, Long id) {
        ReimbursementMailboxConnectionDO mailboxConnection = mailboxConnectionMapper.selectOwnedById(id, userId);
        if (mailboxConnection == null) {
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_MAILBOX_NOT_EXISTS);
        }
        return mailboxConnection;
    }

    /**
     * 转换为不包含邮箱授权码的管理端响应。
     * 
     * @param mailboxConnection 邮箱连接数据
     */
    private ReimbursementMailboxRespVO buildMailboxRespVO(ReimbursementMailboxConnectionDO mailboxConnection) {
        return BeanUtils.toBean(mailboxConnection, ReimbursementMailboxRespVO.class);
    }

}
