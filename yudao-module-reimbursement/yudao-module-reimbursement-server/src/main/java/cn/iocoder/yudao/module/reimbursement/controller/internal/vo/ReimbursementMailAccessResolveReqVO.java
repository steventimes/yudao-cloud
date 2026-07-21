package cn.iocoder.yudao.module.reimbursement.controller.internal.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReimbursementMailAccessResolveReqVO {

    @JsonProperty("mailboxExecutionToken")
    @NotBlank
    private String mailAccessToken;

    @NotBlank
    private String operation;

}
