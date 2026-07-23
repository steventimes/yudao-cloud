package cn.iocoder.yudao.module.reimbursement.service.support;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.module.reimbursement.config.ReimbursementProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import static cn.iocoder.yudao.module.reimbursement.enums.ErrorCodeConstants.REIMBURSEMENT_INTERNAL_TOKEN_INVALID;

/**
 * 报销内部接口 Bearer Token 校验服务。
 */

@Service
@RequiredArgsConstructor
public class ReimbursementInternalAuthService {
    private final ReimbursementProperties properties;


    public void requireAuthorized(String h) {
        String token = h != null && h.startsWith("Bearer ") ? h.substring(7) : "";
        String expected = properties.getInternal().getServiceToken();
        if (expected == null || expected.isEmpty() || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8)))
            throw ServiceExceptionUtil.exception(REIMBURSEMENT_INTERNAL_TOKEN_INVALID);
    }
}
