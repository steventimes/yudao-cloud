package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Data
public class ReimbursementAiFillReqVO {
    private Long tenantId;
    private Long reimbursementId;
    private String reason;
    private String currency;
    private BigDecimal aiConfidence;
    private List<String> warnings;
    private List<Item> items;

    @Data
    public static class Item {
        private String clientItemId;
        private LocalDate expenseDate;
        private String expenseType;
        private String merchantName;
        private BigDecimal amount;
        private BigDecimal taxAmount;
        private String invoiceNumber;
        private String remark;
        private BigDecimal aiConfidence;
        private List<String> attachmentExternalArtifactIds;
    }
}
