package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 请求参数。
 */

@Data
public class ReimbursementMailAccessResolveReqVO {

    /** 邮箱访问令牌。 */
    @JsonProperty("mailboxExecutionToken")
    @NotBlank
    private String mailAccessToken;

    /** 字段 operation。 */
    @NotBlank
    private String operation;

}
