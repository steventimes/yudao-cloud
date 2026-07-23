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
 */
@TableName("reimbursement_mailbox_connection")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReimbursementMailboxConnectionDO extends TenantBaseDO {
    /** 编号。 */
    @TableId
    private Long id;
    private Long ownerUserId;
    private String providerCode;
    private String emailNormalized;
    /** 邮箱用户名。 */
    private String username;
    private String imapHost;
    private Integer imapPort;
    private String tlsVerification;
    /** 加密存储的邮箱授权码。 */
    @ToString.Exclude
    private String credentialCiphertext;
    /** 状态。 */
    private Integer status;
    private LocalDateTime verifiedAt;
    private String lastFailureMessage;

}
