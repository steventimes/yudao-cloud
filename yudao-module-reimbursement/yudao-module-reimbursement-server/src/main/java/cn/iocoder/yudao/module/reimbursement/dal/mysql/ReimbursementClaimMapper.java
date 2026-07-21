package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementClaimDO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.claim.ReimbursementClaimPageReqVO;

@Mapper
public interface ReimbursementClaimMapper extends BaseMapperX<ReimbursementClaimDO> {
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
}
