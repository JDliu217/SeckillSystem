package cn.net.susan.seckill.business.service;

import cn.net.susan.entity.seckill.SeckillProductDetailEntity;
import cn.net.susan.enums.OrderStatusEnum;
import cn.net.susan.seckill.business.entity.seckill.SeckillOrderTradeEntity;
import cn.net.susan.seckill.business.mapper.seckill.SeckillOrderTradeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 秒杀订单 service
 *
 */
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


    private String getOrderStatusString(Integer orderStatus) {
        for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
            if (orderStatusEnum.getValue().equals(orderStatus)) {
                return orderStatusEnum.getDesc();
            }
        }
        return NOT_FOUND_ORDER_STATUS;
    }
}
