package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementItemDO;

@Mapper
public interface ReimbursementItemMapper extends BaseMapperX<ReimbursementItemDO> {
    default List<ReimbursementItemDO> selectListByReimbursementId(Long reimbursementId) {
        return selectList(ReimbursementItemDO::getReimbursementId, reimbursementId);
    }

    default void deleteByReimbursementId(Long reimbursementId) {
        delete(new LambdaQueryWrapperX<ReimbursementItemDO>().eq(ReimbursementItemDO::getReimbursementId,
                reimbursementId));
    }
}
