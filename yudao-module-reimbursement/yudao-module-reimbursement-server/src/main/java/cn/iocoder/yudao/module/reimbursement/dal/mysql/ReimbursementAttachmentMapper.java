package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementAttachmentDO;

/**
 * ReimbursementAttachmentMapper，数据库访问 Mapper。
 */

@Mapper
public interface ReimbursementAttachmentMapper extends BaseMapperX<ReimbursementAttachmentDO> {
    /**
     * 查询数据。
     * 
     * @param reimbursementId 报销单编号
     * @return 处理结果
     */
    default List<ReimbursementAttachmentDO> selectListByReimbursementId(Long reimbursementId) {
        return selectList(ReimbursementAttachmentDO::getReimbursementId, reimbursementId);
    }

    /**
     * 按报销单和外部附件编号查询附件，确保附件属于当前报销单。
     *
     * @param reimbursementId    报销单编号
     * @param externalArtifactId 外部附件存储编号
     * @return 匹配的附件，不存在时返回 {@code null}
     */
    default ReimbursementAttachmentDO selectByExternalArtifactId(Long reimbursementId, String externalArtifactId) {
        return selectOne(new LambdaQueryWrapperX<ReimbursementAttachmentDO>()
                .eq(ReimbursementAttachmentDO::getReimbursementId, reimbursementId)
                .eq(ReimbursementAttachmentDO::getExternalArtifactId, externalArtifactId));
    }

    /**
     * 统计报销单关联的附件数量。
     *
     * @param reimbursementId 报销单编号
     * @return 附件数量
     */
    default long selectCountByReimbursementId(Long reimbursementId) {
        return selectCount(ReimbursementAttachmentDO::getReimbursementId, reimbursementId);
    }

    /**
     * 清空报销单下附件的明细关联，避免删除明细后留下无效外键引用。
     *
     * @param reimbursementId 报销单编号
     */
    @Update("""
            UPDATE reimbursement_attachment
            SET item_id = NULL
            WHERE reimbursement_id = #{reimbursementId}
              AND tenant_id = #{tenantId}
              AND deleted = b'0'
            """)
    int clearItemIdByReimbursementId(@Param("reimbursementId") Long reimbursementId,
            @Param("tenantId") Long tenantId);

    /**
     * 更新指定附件所属的报销明细。
     *
     * @param reimbursementId    报销单编号
     * @param externalArtifactId 外部附件存储编号
     * @param itemId             报销明细编号，可为 {@code null} 表示解除关联
     */
    default void updateItemId(Long reimbursementId, String externalArtifactId, Long itemId) {
        ReimbursementAttachmentDO d = new ReimbursementAttachmentDO();
        d.setItemId(itemId);
        update(d,
                new LambdaQueryWrapperX<ReimbursementAttachmentDO>()
                        .eq(ReimbursementAttachmentDO::getReimbursementId, reimbursementId)
                        .eq(ReimbursementAttachmentDO::getExternalArtifactId, externalArtifactId));
    }
}
