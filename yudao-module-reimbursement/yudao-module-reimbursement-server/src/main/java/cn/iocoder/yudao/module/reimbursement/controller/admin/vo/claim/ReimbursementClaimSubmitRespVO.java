package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * 报销审批提交结果。
 */

@Data
public class ReimbursementClaimSubmitRespVO {
    /** 报销单编号。 */
    private Long reimbursementId;
    /** 状态。 */
    private Integer status;
    /** 流程实例编号。 */
    private String processInstanceId;
}
