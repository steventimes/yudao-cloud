package cn.iocoder.yudao.module.reimbursement.enums;

import cn.iocoder.yudao.framework.common.enums.RpcConstants;

/**
 * 报销模块 RPC 接口常量。
 */
public final class ApiConstants {
    /** RPC 服务名称。 */
    public static final String NAME = "reimbursement-server";
    /** RPC 接口路径前缀。 */
    public static final String PREFIX = RpcConstants.RPC_API_PREFIX + "/reimbursement";
    /** RPC 接口版本号。 */
    public static final String VERSION = "1.0.0";

    /** 工具类私有构造方法，禁止实例化。 */
    private ApiConstants() {
    }
}
