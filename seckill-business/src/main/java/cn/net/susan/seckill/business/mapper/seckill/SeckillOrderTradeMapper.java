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

    /**
     * 关闭超时未支付订单（CAS：只有待支付状态的订单才关得动）
     *
     * <p>用 order_status 本身做乐观锁，不加 version 字段。
     * 同一订单被重复消费或与支付回调并发时，只有一方能拿到影响行数 1。</p>
     *
     * @param id 秒杀订单ID
     * @return 影响行数，1-关单成功，0-状态已变更
     */
    int closeTimeoutOrder(@Param("id") Long id);

    /**
     * 标记订单已支付（CAS：只有待支付状态的订单才改得动）
     *
     * @param id 秒杀订单ID
     * @return 影响行数，1-置为已支付，0-状态已变更
     */
    int markOrderPaid(@Param("id") Long id);

    /**
     * 查询超过支付时限仍未支付的订单ID，供扫表兜底补发关单
     *
     * @param deadline 支付截止时间，order_time 早于该时间视为超时
     * @param limit    单批数量
     * @return 订单ID列表
     */
    List<Long> findTimeoutOrderIds(@Param("deadline") java.util.Date deadline, @Param("limit") int limit);
}
