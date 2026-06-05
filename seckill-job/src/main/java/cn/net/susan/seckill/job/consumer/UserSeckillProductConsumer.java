package cn.net.susan.seckill.job.consumer;

import cn.hutool.json.JSONUtil;
import cn.net.susan.seckill.business.entity.seckill.UserSeckillProductEntity;
import cn.net.susan.seckill.business.service.UserSeckillProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 用户秒杀商品consumer
 *
 */
@RocketMQMessageListener(topic = "${seckill.seckillProductTopic:SECKILL_PRODUCT_TOPIC}",
        consumerGroup = "${seckill.seckillProductGroupc:SECKILL_PRODUCT_GROUP}")
@Slf4j
@Component
public class UserSeckillProductConsumer implements RocketMQListener<MessageExt> {

    @Autowired
    private UserSeckillProductService userSeckillProductService;

    @Override
    public void onMessage(MessageExt message) {
        byte[] body = message.getBody();
        String content = new String(body);
        log.info("UserSeckillProductConsumer接收到消息：{}", content);
        UserSeckillProductEntity userSeckillProductEntity = JSONUtil.toBean(content, UserSeckillProductEntity.class);
        userSeckillProductService.createOrder(userSeckillProductEntity);
    }
}
