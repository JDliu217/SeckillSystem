package cn.net.susan.seckill.job.consumer;

import cn.hutool.json.JSONUtil;
import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTimeoutMessage;
import cn.net.susan.seckill.business.service.SeckillOrderTimeoutService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 订单超时关单 consumer
 *
 * <p>同一条延迟消息可能被投递多次，关单靠 order_status 的 CAS 保证只生效一次，
 * 这里不需要再加分布式锁。</p>
 *
 */
@RocketMQMessageListener(topic = "${seckill.orderTimeoutTopic:seckill.order.timeout.topic}",
        consumerGroup = "${seckill.orderTimeoutGroup:seckill-order-timeout-group}")
@Slf4j
@Component
public class SeckillOrderTimeoutConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private SeckillOrderTimeoutService seckillOrderTimeoutService;

    @Override
    public void onMessage(MessageExt message) {
        byte[] body = message.getBody();
        String content = new String(body);
        log.info("SeckillOrderTimeoutConsumer接收到消息：{}", content);
        SeckillOrderTimeoutMessage timeoutMessage = JSONUtil.toBean(content, SeckillOrderTimeoutMessage.class);
        if (timeoutMessage == null || timeoutMessage.getOrderId() == null) {
            log.warn("订单超时消息格式不对，直接丢弃：{}", content);
            return;
        }
        seckillOrderTimeoutService.handleOrderTimeout(timeoutMessage.getOrderId());
    }
}
