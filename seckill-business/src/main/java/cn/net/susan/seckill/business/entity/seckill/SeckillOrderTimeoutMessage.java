package cn.net.susan.seckill.business.entity.seckill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单超时消息
 *
 * <p>下单成功后投递一条延迟消息，到期仍未支付则关单并回补库存。</p>
 *
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeckillOrderTimeoutMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 秒杀订单ID
     */
    private Long orderId;

    /**
     * 秒杀商品ID（关单后回补预扣库存用，随消息带上省一次查库）
     */
    private Long seckillProductId;
}
