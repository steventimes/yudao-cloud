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
public class ReimbursementClaimSubmitReqVO {
    /** 编号。 */
    @NotNull
    private Long id;
    private Map<String, List<Long>> startUserSelectAssignees;
}
