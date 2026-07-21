package cn.iocoder.yudao.module.reimbursement.enums;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;

/**
 * ApiConstants，枚举定义。
 */
public final class ApiConstants {
    public static final String NAME = "reimbursement-server";
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/reimbursement";
    public static final String VERSION = "1.0.0";

    /** 工具类私有构造方法，禁止实例化。 */
    private ApiConstants() {
    }
}
