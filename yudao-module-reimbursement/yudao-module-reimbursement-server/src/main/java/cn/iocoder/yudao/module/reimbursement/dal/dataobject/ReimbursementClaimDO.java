package cn.iocoder.yudao.module.reimbursement.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报销申请 DO
 * 
 * @author Codex
 */
@TableName("reimbursement_claim")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReimbursementClaimDO extends TenantBaseDO {
    /** 编号。 */
    @TableId
    private Long id;
    /** 报销单号。 */
    private String reimbursementNo;
    /** 用户编号。 */
    private Long userId;
    /** 部门编号。 */
    private Long deptId;
    /** 申请人名称快照。 */
    private String applicantNameSnapshot;
    /** 报销事由。 */
    private String reason;
    /** 报销总金额。 */
    private BigDecimal totalAmount;
    /** 币种。 */
    private String currency;
    /** 状态。 */
    private Integer status;
    /** 流程实例编号。 */
    private String processInstanceId;
    /** 数据来源。 */
    private String source;
    /** 邮箱连接编号。 */
    private Long mailboxConnectionId;
    /** AI 识别置信度。 */
    private BigDecimal aiConfidence;
    /** AI 处理失败原因。 */
    private String aiFailureMessage;
    /** 提交时间。 */
    private LocalDateTime submitTime;

}
