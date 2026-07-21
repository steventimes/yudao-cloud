package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * 请求参数。
 */

@Data
public class ReimbursementItemReqVO {
    /** 明细客户端编号。 */
    private String clientItemId;
    /** 费用日期。 */
    @NotNull
    private LocalDate expenseDate;
    /** 费用类型。 */
    @NotBlank
    private String expenseType;
    /** 商户名称。 */
    private String merchantName;
    /** 金额。 */
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal amount;
    /** 税额。 */
    @DecimalMin("0")
    private BigDecimal taxAmount;
    /** 发票号码。 */
    private String invoiceNumber;
    /** 备注。 */
    private String remark;
}
