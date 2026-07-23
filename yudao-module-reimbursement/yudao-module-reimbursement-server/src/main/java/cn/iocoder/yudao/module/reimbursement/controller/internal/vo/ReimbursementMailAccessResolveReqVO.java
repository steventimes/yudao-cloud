package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 邮箱短期访问令牌解析请求。
 */

@Data
public class ReimbursementMailAccessResolveReqVO {

    /** 邮箱访问令牌。 */
    @JsonProperty("mailboxExecutionToken")
    @NotBlank
    private String mailAccessToken;

    /** 本次请求的邮箱操作，仅允许 SEARCH 或 FETCH。 */
    @NotBlank
    private String operation;

}
