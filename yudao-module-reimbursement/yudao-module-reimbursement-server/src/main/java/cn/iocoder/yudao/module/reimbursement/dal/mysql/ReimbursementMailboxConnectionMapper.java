package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.ReimbursementMailboxPageReqVO;

@Mapper
public interface ReimbursementMailboxConnectionMapper extends BaseMapperX<ReimbursementMailboxConnectionDO> {
    default PageResult<ReimbursementMailboxConnectionDO> selectPage(Long ownerUserId,
            ReimbursementMailboxPageReqVO reqVO) {
        return selectPage(reqVO,
                new LambdaQueryWrapperX<ReimbursementMailboxConnectionDO>()
                        .eq(ReimbursementMailboxConnectionDO::getOwnerUserId, ownerUserId)
                        .eqIfPresent(ReimbursementMailboxConnectionDO::getStatus, reqVO.getStatus())
                        .eqIfPresent(ReimbursementMailboxConnectionDO::getProviderCode, reqVO.getProviderCode())
                        .likeIfPresent(ReimbursementMailboxConnectionDO::getEmailNormalized, reqVO.getEmail())
                        .orderByDesc(ReimbursementMailboxConnectionDO::getId));
    }

    default ReimbursementMailboxConnectionDO selectOwnedById(Long id, Long ownerUserId) {
        return selectOne(new LambdaQueryWrapperX<ReimbursementMailboxConnectionDO>()
                .eq(ReimbursementMailboxConnectionDO::getId, id)
                .eq(ReimbursementMailboxConnectionDO::getOwnerUserId, ownerUserId));
    }
}
