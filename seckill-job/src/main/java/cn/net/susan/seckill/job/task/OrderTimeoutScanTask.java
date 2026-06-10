package cn.net.susan.seckill.job.task;

import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import cn.net.susan.seckill.business.service.SeckillOrderTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 超时订单扫表兜底任务
 *
 * <p>延迟消息丢了或者消费失败时，靠这个任务把漏掉的订单捞回来，
 * 走的是和消费者同一套 {@link SeckillOrderTimeoutService#handleOrderTimeout}，
 * 因此关单与回补的判定完全一致。</p>
 *
 * <p>多实例部署时几个实例会同时扫到同一批订单，靠关单 SQL 的 CAS 保证只关一次。</p>
 *
 * @since 1.0.0
 */
@Slf4j
@Component
public class OrderTimeoutScanTask {

    /**
     * 单批最多处理多少条，避免一次扫太多把库拖慢
     */
    private static final int BATCH_LIMIT = 200;

    @Autowired
    private SeckillOrderTradeMapper seckillOrderTradeMapper;

    @Autowired
    private SeckillOrderTimeoutService seckillOrderTimeoutService;

    /**
     * 支付超时时间（秒），与延迟消息的等级保持同一个口径
     */
    @Value("${seckill.orderTimeoutSeconds:600}")
    private int orderTimeoutSeconds;

    /**
     * 每分钟扫一次待支付且已超时的订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanTimeoutOrders() {
        Date deadline = new Date(System.currentTimeMillis() - orderTimeoutSeconds * 1000L);
        List<Long> orderIds = seckillOrderTradeMapper.findTimeoutOrderIds(deadline, BATCH_LIMIT);
        if (CollectionUtils.isEmpty(orderIds)) {
            return;
        }

        int closed = 0;
        for (Long orderId : orderIds) {
            try {
                if (seckillOrderTimeoutService.handleOrderTimeout(orderId)) {
                    closed++;
                }
            } catch (Exception e) {
                // 单条失败不影响后面的，下一分钟还会再扫到
                log.error("扫表关单失败，orderId={}", orderId, e);
            }
        }
        log.info("超时订单扫描完成：候选={}, 实际关单={}", orderIds.size(), closed);
    }
}
