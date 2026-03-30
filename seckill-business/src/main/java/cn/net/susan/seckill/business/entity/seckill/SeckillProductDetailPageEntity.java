package cn.net.susan.seckill.business.entity.seckill;

import cn.net.susan.entity.seckill.SeckillProductDetailEntity;
import lombok.Data;

/**
 * 秒杀商品详情页面实体
 *
 */
@Data
public class SeckillProductDetailPageEntity extends SeckillProductDetailEntity {

    /**
     * 还剩多少秒开始倒计时
     */
    private Integer remainSeconds = 0;

    /**
     * 秒杀状态 0：未开始 1：秒杀倒计时 2：秒杀进行中 3：秒杀已结束
     */
    private Integer seckillStatus = 0;
}