package cn.net.susan.seckill.business.mapper.seckill;

import cn.net.susan.seckill.business.entity.seckill.SeckillProductConditionEntity;
import cn.net.susan.seckill.business.entity.seckill.SeckillProductEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 秒杀商品 mapper
 *
 */
public interface SeckillProductMapper {
    /**
     * 查询秒杀商品信息
     *
     * @param id 秒杀商品ID
     * @return 秒杀商品信息
     */
    SeckillProductEntity findById(Long id);

    /**
     * 添加秒杀商品
     *
     * @param seckillProductEntity 秒杀商品信息
     * @return 结果
     */
    int insert(SeckillProductEntity seckillProductEntity);

    /**
     * 修改秒杀商品
     *
     * @param seckillProductEntity 秒杀商品信息
     * @return 结果
     */
    int update(SeckillProductEntity seckillProductEntity);

    /**
     * 扣减预扣库存
     *
     * @param id 秒杀商品信息ID
     * @return 结果
     */
    int reduceWithHoldStock(@Param("id") Long id);

    /**
     * 批量删除秒杀商品
     *
     * @param ids    id集合
     * @param entity 秒杀商品实体
     * @return 结果
     */
    int deleteByIds(@Param("ids") List<Long> ids, @Param("entity") SeckillProductEntity entity);

    /**
     * 批量查询秒杀商品信息
     *
     * @param ids ID集合
     * @return 部门信息
     */
    List<SeckillProductEntity> findByIds(List<Long> ids);

    /**
     * 根据条件查询秒杀商品
     *
     * @param seckillProductConditionEntity 查询条件
     * @return 秒杀商品列表
     */
    List<SeckillProductEntity> searchByCondition(SeckillProductConditionEntity seckillProductConditionEntity);

    /**
     * 根据条件查询秒杀商品数量
     *
     * @param seckillProductConditionEntity 查询条件
     * @return 数量
     */
    int searchCount(SeckillProductConditionEntity seckillProductConditionEntity);
}
