package cn.iocoder.yudao.module.reimbursement.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 报销附件 DO
 *
 * @author Codex
 */
@TableName("reimbursement_attachment")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReimbursementAttachmentDO extends TenantBaseDO {

    /** 编号 */
    @TableId
    private Long id;
    /** 报销编号 */
    private Long reimbursementId;
    /** 关联明细编号 */
    private Long itemId;
    /** 邮件附件稳定编号 */
    private String externalArtifactId;
    /** 文件 URL */
    private String fileUrl;
    /** 文件名 */
    private String fileName;
    /** MIME 类型 */
    private String mimeType;
    /** 文件大小 */
    private Long size;
    /** SHA-256 */
    private String sha256;
    /** 单据类型 */
    private String documentType;

}
