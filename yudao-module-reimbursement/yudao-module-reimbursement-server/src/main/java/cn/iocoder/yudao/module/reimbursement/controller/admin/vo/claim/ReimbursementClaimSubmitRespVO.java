package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Data
public class ReimbursementClaimSubmitRespVO {
    private Long reimbursementId;
    private Integer status;
    private String processInstanceId;
}
