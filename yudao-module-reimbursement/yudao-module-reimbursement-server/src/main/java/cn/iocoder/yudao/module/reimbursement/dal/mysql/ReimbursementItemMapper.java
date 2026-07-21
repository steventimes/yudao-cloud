package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementItemDO;

/**
 * ReimbursementItemMapper，数据库访问 Mapper。
 */

@Mapper
public interface ReimbursementItemMapper extends BaseMapperX<ReimbursementItemDO> {
    /**
     * 查询数据。
     * 
     * @param reimbursementId 报销单编号
     * @return 处理结果
     */
    default List<ReimbursementItemDO> selectListByReimbursementId(Long reimbursementId) {
        return selectList(ReimbursementItemDO::getReimbursementId, reimbursementId);
    }

    /**
     * 删除报销单下的全部明细，供人工重建或 AI 重建明细前清理旧数据。
     *
     * @param reimbursementId 报销单编号
     */
    default void deleteByReimbursementId(Long reimbursementId) {
        delete(new LambdaQueryWrapperX<ReimbursementItemDO>().eq(ReimbursementItemDO::getReimbursementId,
                reimbursementId));
    }
}
