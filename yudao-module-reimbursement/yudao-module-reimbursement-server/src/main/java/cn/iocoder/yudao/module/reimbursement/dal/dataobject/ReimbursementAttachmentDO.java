package cn.iocoder.yudao.module.reimbursement.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 报销附件 DO
 */
@TableName("reimbursement_attachment")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReimbursementAttachmentDO extends TenantBaseDO {
    /** 编号。 */
    @TableId
    private Long id;
    /** 报销单编号。 */
    private Long reimbursementId;
    /** 明细编号。 */
    private Long itemId;
    private String externalArtifactId;
    private String fileUrl;
    private String fileName;
    private String mimeType;
    private Long size;
    private String sha256;
    private String documentType;

}
