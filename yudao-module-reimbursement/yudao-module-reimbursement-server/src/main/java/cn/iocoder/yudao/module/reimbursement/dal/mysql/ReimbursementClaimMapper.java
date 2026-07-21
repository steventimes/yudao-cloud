package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimPageReqVO;

/**
 * ReimbursementClaimMapper，数据库访问 Mapper。
 */

@Mapper
public interface ReimbursementClaimMapper extends BaseMapperX<ReimbursementClaimDO> {
    /**
     * 查询数据。
     * 
     * @param userId   用户编号
     * @param queryAll 是否查询全部数据
     * @param reqVO    请求参数对象
     * @return 处理结果
     */
    default PageResult<ReimbursementClaimDO> selectPage(Long userId, boolean queryAll,
            ReimbursementClaimPageReqVO reqVO) {
        LambdaQueryWrapperX<ReimbursementClaimDO> q = new LambdaQueryWrapperX<ReimbursementClaimDO>();
        if (!queryAll) {
            q.eq(ReimbursementClaimDO::getUserId, userId);
        }
        return selectPage(reqVO,
                q.eqIfPresent(ReimbursementClaimDO::getStatus, reqVO.getStatus())
                        .eqIfPresent(ReimbursementClaimDO::getSource, reqVO.getSource())
                        .likeIfPresent(ReimbursementClaimDO::getReason, reqVO.getReason())
                        .betweenIfPresent(ReimbursementClaimDO::getCreateTime, reqVO.getCreateTime())
                        .orderByDesc(ReimbursementClaimDO::getId));
    }

    /**
     * 查询指定用户拥有的报销单，防止越权访问其他用户的数据。
     *
     * @param id     报销单编号
     * @param userId 报销单所属用户编号
     * @return 匹配的报销单，不存在时返回 {@code null}
     */
    default ReimbursementClaimDO selectOwnedById(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ReimbursementClaimDO>().eq(ReimbursementClaimDO::getId, id)
                .eq(ReimbursementClaimDO::getUserId, userId));
    }

    /**
     * 按查询权限读取报销单；管理员查询全部数据，普通用户只能查询本人数据。
     *
     * @param id       报销单编号
     * @param userId   当前用户编号
     * @param queryAll 是否跳过用户归属过滤
     * @return 匹配的报销单，不存在时返回 {@code null}
     */
    default ReimbursementClaimDO selectByIdForUser(Long id, Long userId, boolean queryAll) {
        LambdaQueryWrapperX<ReimbursementClaimDO> query = new LambdaQueryWrapperX<ReimbursementClaimDO>()
                .eq(ReimbursementClaimDO::getId, id);
        if (!queryAll) {
            query.eq(ReimbursementClaimDO::getUserId, userId);
        }
        return selectOne(query);
    }
}
