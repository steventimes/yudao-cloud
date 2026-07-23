package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * 报销费用明细响应。
 */

@Data
public class ReimbursementItemRespVO {
    /** 编号。 */
    private Long id;
    /** 明细客户端编号。 */
    private String clientItemId;
    /** 费用日期。 */
    private LocalDate expenseDate;
    /** 费用类型。 */
    private String expenseType;
    /** 商户名称。 */
    private String merchantName;
    /** 金额。 */
    private BigDecimal amount;
    /** 税额。 */
    private BigDecimal taxAmount;
    /** 发票号码。 */
    private String invoiceNumber;
    /** 备注。 */
    private String remark;
    /** AI 识别置信度。 */
    private BigDecimal aiConfidence;
    private Boolean manuallyModified;
}
