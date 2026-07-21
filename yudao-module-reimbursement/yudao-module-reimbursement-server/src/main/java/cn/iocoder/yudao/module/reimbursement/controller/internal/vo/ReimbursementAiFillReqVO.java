package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 报销 AI 回填请求参数。
 */

@Data
public class ReimbursementAiFillReqVO {
    /** 租户编号。 */
    @NotNull
    @Positive
    private Long tenantId;
    /** 报销单编号。 */
    @NotNull
    @Positive
    private Long reimbursementId;
    /** 报销事由。 */
    private String reason;
    /** 币种。 */
    private String currency;
    /** AI 识别置信度。 */
    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private BigDecimal aiConfidence;
    /** 识别警告列表。 */
    private List<String> warnings;
    /** 报销明细列表。 */
    @NotEmpty
    @Valid
    private List<Item> items;

    /** AI 识别出的报销明细。 */
    @Data
    public static class Item {
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
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal amount;
        /** 税额。 */
        @DecimalMin("0.0")
        private BigDecimal taxAmount;
        /** 发票号码。 */
        private String invoiceNumber;
        /** 备注。 */
        private String remark;
        /** AI 识别置信度。 */
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private BigDecimal aiConfidence;
        /** 外部附件产物编号列表。 */
        private List<String> attachmentExternalArtifactIds;
    }
}
