package cn.net.susan.seckill.business.service;

import cn.net.susan.entity.seckill.SeckillProductDetailEntity;
import cn.net.susan.enums.OrderStatusEnum;
import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 秒杀订单 service
 *
 */
@Slf4j
@Service
public class SeckillOrderTradeService {
    private static final String NOT_FOUND_ORDER_STATUS = "未知";

    @Autowired
    private SeckillOrderTradeMapper seckillOrderTradeMapper;
    @Autowired
    private ProductService productService;

    /**
     * 获取秒杀订单详情
     *
     * @param id 秒杀订单ID
     * @return 秒杀订单详情
     */
    public SeckillOrderTradeEntity getOrderDetail(Long id) {
        SeckillOrderTradeEntity orderDetail = seckillOrderTradeMapper.findById(id);
        if (Objects.nonNull(orderDetail)) {
            SeckillProductDetailEntity productDetail = productService.getProductDetail(orderDetail.getSeckillProductId());
            if (Objects.nonNull(productDetail)) {
                orderDetail.setCover(productDetail.getCover());
            } else {
                orderDetail.setCover("");
            }
            orderDetail.setOrderStatusString(getOrderStatusString(orderDetail.getOrderStatus()));
            return orderDetail;
        }
        return null;
    }


    /**
     * 支付回调：把待支付订单置为已支付
     *
     * <p>与超时关单互斥，两边都用 order_status 做 CAS，只有一方能成功。</p>
     *
     * @param orderId 秒杀订单ID
     * @return true-本次置为已支付；false-订单已支付或已被关闭
     */
    public boolean markPaid(Long orderId) {
        if (Objects.isNull(orderId)) {
            log.warn("支付回调失败：orderId 为空");
            return false;
        }

        int count = seckillOrderTradeMapper.markOrderPaid(orderId);
        if (count != 1) {
            // 已经支付过，或者已经被超时任务关掉，都属于正常情况，不抛异常
            log.info("支付回调未命中待支付订单，orderId={}", orderId);
            return false;
        }
        log.info("支付回调处理成功，orderId={}", orderId);
        return true;
    }

    private String getOrderStatusString(Integer orderStatus) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (orderStatusEnum.getValue().equals(orderStatus)) {
                return orderStatusEnum.getDesc();
            }
        }
        return NOT_FOUND_ORDER_STATUS;
    }
}
