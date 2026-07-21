package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Mapper;
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

    default ReimbursementAttachmentDO selectByExternalArtifactId(Long reimbursementId, String externalArtifactId) {
        return selectOne(new LambdaQueryWrapperX<ReimbursementAttachmentDO>()
                .eq(ReimbursementAttachmentDO::getReimbursementId, reimbursementId)
                .eq(ReimbursementAttachmentDO::getExternalArtifactId, externalArtifactId));
    }

    default long selectCountByReimbursementId(Long reimbursementId) {
        return selectCount(ReimbursementAttachmentDO::getReimbursementId, reimbursementId);
    }

    default void updateItemId(Long reimbursementId, String externalArtifactId, Long itemId) {
        ReimbursementAttachmentDO d = new ReimbursementAttachmentDO();
        d.setItemId(itemId);
        update(d,
                new LambdaQueryWrapperX<ReimbursementAttachmentDO>()
                        .eq(ReimbursementAttachmentDO::getReimbursementId, reimbursementId)
                        .eq(ReimbursementAttachmentDO::getExternalArtifactId, externalArtifactId));
    }
}
