package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementItemRespVO {
    private Long id;
    private String clientItemId;
    private LocalDate expenseDate;
    private String expenseType;
    private String merchantName;
    private BigDecimal amount;
    private BigDecimal taxAmount;
    private String invoiceNumber;
    private String remark;
    private BigDecimal aiConfidence;
    private Boolean manuallyModified;
}
