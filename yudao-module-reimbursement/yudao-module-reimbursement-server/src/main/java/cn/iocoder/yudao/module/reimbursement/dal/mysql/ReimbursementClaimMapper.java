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

    default ReimbursementClaimDO selectOwnedById(Long id, Long userId) {
        return selectOne(new LambdaQueryWrapperX<ReimbursementClaimDO>().eq(ReimbursementClaimDO::getId, id)
                .eq(ReimbursementClaimDO::getUserId, userId));
    }

    default ReimbursementClaimDO selectByIdForUser(Long id, Long userId, boolean queryAll) {
        LambdaQueryWrapperX<ReimbursementClaimDO> query = new LambdaQueryWrapperX<ReimbursementClaimDO>()
                .eq(ReimbursementClaimDO::getId, id);
        if (!queryAll) {
            query.eq(ReimbursementClaimDO::getUserId, userId);
        }
        return selectOne(query);
    }
}
