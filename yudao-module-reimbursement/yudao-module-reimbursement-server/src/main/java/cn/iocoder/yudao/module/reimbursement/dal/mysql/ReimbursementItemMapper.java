package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementItemDO;

/**
 * 报销费用明细数据库访问 Mapper。
 */

@Mapper
public interface ReimbursementItemMapper extends BaseMapperX<ReimbursementItemDO> {
    default List<ReimbursementItemDO> selectListByReimbursementId(Long reimbursementId) {
        return selectList(ReimbursementItemDO::getReimbursementId, reimbursementId);
    }

    /**
     * 删除报销单下的全部明细，供人工重建或 AI 重建明细前清理旧数据。
     *
     * @param reimbursementId 报销单编号
     */
    @Delete("""
            DELETE FROM reimbursement_item
            WHERE reimbursement_id = #{reimbursementId}
            AND tenant_id = #{tenantId}
            """)
    int deletePermanentlyByReimbursementId(@Param("reimbursementId") Long reimbursementId,
            @Param("tenantId") Long tenantId);
}
