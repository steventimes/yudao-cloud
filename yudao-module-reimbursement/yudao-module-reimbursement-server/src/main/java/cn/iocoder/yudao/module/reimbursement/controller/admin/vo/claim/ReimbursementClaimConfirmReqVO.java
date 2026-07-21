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
public class ReimbursementClaimConfirmReqVO {
    /** 编号。 */
    @NotNull
    private Long id;
    /** 报销事由。 */
    @NotBlank
    private String reason;
    /** 币种。 */
    private String currency = "CNY";
    /** 报销明细列表。 */
    @NotEmpty
    @Valid
    private List<ReimbursementItemReqVO> items;
}
