package cn.iocoder.yudao.module.reimbursement.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.*;
import cn.iocoder.yudao.module.reimbursement.dal.dataobject.ReimbursementMailboxConnectionDO;
import cn.iocoder.yudao.module.reimbursement.controller.admin.vo.mailbox.ReimbursementMailboxPageReqVO;

/**
 * 报销邮箱连接数据库访问 Mapper。
 */

@Mapper
public interface ReimbursementMailboxConnectionMapper extends BaseMapperX<ReimbursementMailboxConnectionDO> {
    /** 物理删除指定租户和用户拥有的邮箱绑定，避免凭据记录残留。 */
    @Delete("""
            DELETE FROM reimbursement_mailbox_connection
            WHERE id = #{id}
            AND tenant_id = #{tenantId}
            AND owner_user_id = #{ownerUserId}
            """)
    int deletePermanently(@Param("id") Long id, @Param("tenantId") Long tenantId,
            @Param("ownerUserId") Long ownerUserId);

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

    /**
     * 查询指定用户拥有的邮箱绑定，防止越权读取其他用户的邮箱凭据。
     *
     * @param id          邮箱绑定编号
     * @param ownerUserId 邮箱绑定所属用户编号
     * @return 匹配的邮箱绑定，不存在时返回 {@code null}
     */
    default ReimbursementMailboxConnectionDO selectOwnedById(Long id, Long ownerUserId) {
        return selectOne(new LambdaQueryWrapperX<ReimbursementMailboxConnectionDO>()
                .eq(ReimbursementMailboxConnectionDO::getId, id)
                .eq(ReimbursementMailboxConnectionDO::getOwnerUserId, ownerUserId));
    }
}
