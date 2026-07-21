package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementClaimSubmitReqVO {
    @NotNull
    private Long id;
    private Map<String, List<Long>> startUserSelectAssignees;
}
