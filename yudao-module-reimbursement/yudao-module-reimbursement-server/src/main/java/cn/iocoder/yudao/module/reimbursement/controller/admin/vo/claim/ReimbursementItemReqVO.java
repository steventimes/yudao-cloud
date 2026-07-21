package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementItemReqVO {
    private String clientItemId;
    @NotNull
    private LocalDate expenseDate;
    @NotBlank
    private String expenseType;
    private String merchantName;
    @NotNull
    @DecimalMin(value = "0", inclusive = false)
    private BigDecimal amount;
    @DecimalMin("0")
    private BigDecimal taxAmount;
    private String invoiceNumber;
    private String remark;
}
