package cn.net.susan.seckill.business.mapper.seckill;

import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeConditionEntity;
import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 秒杀订单 mapper
 *
 */
public interface SeckillOrderTradeMapper {
    /**
     * 查询秒杀订单信息
     *
     * @param id 秒杀订单ID
     * @return 秒杀订单信息
     */
    SeckillOrderTradeEntity findById(Long id);

    /**
     * 添加秒杀订单
     *
     * @param seckillOrderTradeEntity 秒杀订单信息
     * @return 结果
     */
    int insert(SeckillOrderTradeEntity seckillOrderTradeEntity);

    /**
     * 修改秒杀订单
     *
     * @param seckillOrderTradeEntity 秒杀订单信息
     * @return 结果
     */
    int update(SeckillOrderTradeEntity seckillOrderTradeEntity);

    /**
     * 批量删除秒杀订单
     *
     * @param ids    id集合
     * @param entity 秒杀订单实体
     * @return 结果
     */
    int deleteByIds(@Param("ids") List<Long> ids, @Param("entity") SeckillOrderTradeEntity entity);

    /**
     * 批量查询秒杀订单信息
     *
     * @param ids ID集合
     * @return 部门信息
     */
    List<SeckillOrderTradeEntity> findByIds(List<Long> ids);

    /**
     * 根据条件查询秒杀订单信息
     *
     * @param seckillOrderTradeConditionEntity 查询条件
     * @return 秒杀订单信息列表
     */
    List<SeckillOrderTradeEntity> searchByCondition(SeckillOrderTradeConditionEntity seckillOrderTradeConditionEntity);

    /**
     * 根据条件查询秒杀订单数量
     *
     * @param seckillOrderTradeConditionEntity 查询条件
     * @return 数量
     */
    int searchCount(SeckillOrderTradeConditionEntity seckillOrderTradeConditionEntity);
}
