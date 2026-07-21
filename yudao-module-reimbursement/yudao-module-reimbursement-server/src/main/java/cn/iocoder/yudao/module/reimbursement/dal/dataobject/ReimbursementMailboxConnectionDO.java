package cn.iocoder.yudao.module.reimbursement.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 用户报销邮箱绑定 DO
 *
 * @author Codex
 */
@TableName("reimbursement_mailbox_connection")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReimbursementMailboxConnectionDO extends TenantBaseDO {

    /** 编号 */
    @TableId
    private Long id;
    /** 所属用户编号 */
    private Long ownerUserId;
    /** 邮箱提供方 */
    private String providerCode;
    /** 规范化邮箱地址 */
    private String emailNormalized;
    /** IMAP 用户名 */
    private String username;
    /** IMAPS 主机 */
    private String imapHost;
    /** IMAPS 端口 */
    private Integer imapPort;
    /** TLS 校验模式 */
    private String tlsVerification;
    /** 加密后的授权码 */
    private String credentialCiphertext;
    /** 验证状态 */
    private Integer status;
    /** 最近验证时间 */
    private LocalDateTime verifiedAt;
    /** 最近失败原因 */
    private String lastFailureMessage;

}
