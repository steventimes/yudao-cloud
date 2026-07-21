package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
public class ReimbursementAiFillReqVO {
    @NotNull
    @Positive
    private Long tenantId;
    @NotNull
    @Positive
    private Long reimbursementId;
    private String reason;
    private String currency;
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal aiConfidence;
    private List<String> warnings;
    @NotEmpty
    @Valid
    private List<Item> items;

    @Data
    public static class Item {
        private String clientItemId;
        @NotNull
        private LocalDate expenseDate;
        @NotBlank
        private String expenseType;
        private String merchantName;
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal amount;
        @DecimalMin("0.0")
        private BigDecimal taxAmount;
        private String invoiceNumber;
        private String remark;
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private BigDecimal aiConfidence;
        private List<String> attachmentExternalArtifactIds;
    }
}
