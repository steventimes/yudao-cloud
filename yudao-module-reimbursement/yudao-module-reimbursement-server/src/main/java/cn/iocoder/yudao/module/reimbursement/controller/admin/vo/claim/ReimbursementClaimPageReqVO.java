package cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 报销单分页查询请求。
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class ReimbursementClaimPageReqVO extends PageParam {
    /** 状态。 */
    private Integer status;
    /** 数据来源。 */
    private String source;
    /** 报销事由。 */
    private String reason;
    /** 创建时间。 */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}
