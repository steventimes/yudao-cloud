package cn.iocoder.yudao.module.reimbursement.dal.dataobject;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报销明细 DO
 *
 * @author Codex
 */
@TableName("reimbursement_item")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReimbursementItemDO extends TenantBaseDO {

    /** 编号 */
    @TableId
    private Long id;
    /** 报销编号 */
    private Long reimbursementId;
    /** 请求内明细编号 */
    private String clientItemId;
    /** 费用日期 */
    private LocalDate expenseDate;
    /** 费用类型 */
    private String expenseType;
    /** 商户名称 */
    private String merchantName;
    /** 金额 */
    private BigDecimal amount;
    /** 税额 */
    private BigDecimal taxAmount;
    /** 发票号码 */
    private String invoiceNumber;
    /** 备注 */
    private String remark;
    /** AI 置信度 */
    private BigDecimal aiConfidence;
    /** 是否人工修改 */
    private Boolean manuallyModified;

}
