package cn.net.susan.seckill.business.service;

import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import cn.net.susan.seckill.business.mapper.seckill.SeckillProductMapper;
import cn.net.susan.seckill.business.util.BusinessKeyUtil;
import cn.net.susan.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 订单超时关单 service
 *
 * <p>延迟消息和扫表兜底都走这里，保证两条入口的行为完全一致。</p>
 *
 * <p>流程固定为「先关单、后回补」：关单用 order_status 做 CAS，
 * 只有拿到影响行数 1 的那一方才有资格回补库存，因此重复投递天然幂等，不需要额外加锁。</p>
 *
 * @since 1.0.0
 */
@Slf4j
@Service
public class SeckillOrderTimeoutService {

    /**
     * 订单状态：待支付
     */
    private static final Integer ORDER_STATUS_UNPAID = 1;

    @Autowired
    private SeckillOrderTradeMapper seckillOrderTradeMapper;

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    @Autowired
    private RedisUtil redisUtil;

    /**
     * 处理订单超时
     *
     * <p>关单与回补在同一个本地事务里（同库同数据源），回补抛异常时关单一起回滚，
     * 订单会回到待支付状态，下一轮扫表还会再捞到它，不会出现「单关了货没还」。</p>
     *
     * @param orderId 秒杀订单ID
     * @return true-本次真的关掉了订单；false-订单不存在 / 已支付 / 已被别的实例关掉
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleOrderTimeout(Long orderId) {
        if (orderId == null) {
            log.warn("关单失败：orderId 为空");
            return false;
        }

        SeckillOrderTradeEntity order = seckillOrderTradeMapper.findById(orderId);
        if (order == null) {
            log.warn("关单跳过：订单不存在，orderId={}", orderId);
            return false;
        }
        if (!ORDER_STATUS_UNPAID.equals(order.getOrderStatus())) {
            log.info("关单跳过：订单不是待支付状态，orderId={}, orderStatus={}",
                    orderId, order.getOrderStatus());
            return false;
        }

        // 第一步：关单。影响行数不是 1 说明已经被支付回调或别的消费者改过，直接返回，不抛异常
        int closed = seckillOrderTradeMapper.closeTimeoutOrder(orderId);
        if (closed != 1) {
            log.info("关单跳过：状态已被变更，orderId={}", orderId);
            return false;
        }

        // 第二步：回补库存。顺序不能反，只有关单成功才允许还库存
        restoreStock(order.getSeckillProductId());
        log.info("订单超时已关闭，orderId={}, seckillProductId={}", orderId, order.getSeckillProductId());
        return true;
    }

    /**
     * 回补预扣库存
     *
     * <p>只动 with_hold_quantity，不动 remain_quantity；上限由 SQL 的
     * {@code with_hold_quantity < remain_quantity} 兜住。</p>
     *
     * @param seckillProductId 秒杀商品ID
     */
    private void restoreStock(Long seckillProductId) {
        if (seckillProductId == null) {
            log.warn("跳过库存回补：秒杀商品ID为空");
            return;
        }

        int restored = seckillProductMapper.restoreWithHoldStock(seckillProductId);
        if (restored != 1) {
            log.warn("库存回补未生效：预扣已等于实剩，seckillProductId={}", seckillProductId);
            return;
        }

        // 数据库还了库存，Redis 计数器也得跟上，否则商品仍然过不了预扣闸门
        String stockKey = BusinessKeyUtil.getProductStockKey(seckillProductId);
        String overKey = BusinessKeyUtil.getProductOverKey(seckillProductId);
        String stockValue = redisUtil.get(stockKey);
        if (!StringUtils.hasLength(stockValue)) {
            // key 不存在时不凭空造一个，避免把没预热过的商品刷出库存
            log.warn("Redis 库存计数器不存在，仅回补数据库，seckillProductId={}", seckillProductId);
            return;
        }

        redisUtil.increment(stockKey);
        // 有库存了就把售罄标记摘掉。注意 api 进程内的本地售罄 Map 不会自动失效，
        // 那台机器要等重启才会重新放行，这是原有的已知行为，本次不动它
        if (StringUtils.hasLength(redisUtil.get(overKey))) {
            redisUtil.del(overKey);
        }
        log.info("预扣库存已回补，seckillProductId={}, stockAfter={}", seckillProductId,
                redisUtil.get(stockKey));
    }
}
