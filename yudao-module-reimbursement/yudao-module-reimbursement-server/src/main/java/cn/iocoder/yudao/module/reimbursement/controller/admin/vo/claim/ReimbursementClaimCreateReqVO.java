package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementClaimCreateReqVO {
    @NotBlank
    private String reason;
    private String currency = "CNY";
    @NotEmpty
    @Valid
    private List<ReimbursementItemReqVO> items;
}
