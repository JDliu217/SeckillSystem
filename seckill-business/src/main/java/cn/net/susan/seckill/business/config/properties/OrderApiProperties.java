package cn.net.susan.seckill.business.config.properties;

import lombok.Data;

/**
 * 订单API接口配置
 *
 */
@Data
public class OrderApiProperties {

    /**
     * host地址
     */
    private String host;

    /**
     * 创建订单url
     */
    private String createOrderUrl;

    /**
     * 查询订单状态
     */
    private String queryOrderStatus;

    /**
     * 密钥
     */
    private String secretKey;
}
