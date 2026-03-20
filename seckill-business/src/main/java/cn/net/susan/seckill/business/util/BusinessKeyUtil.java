package cn.net.susan.seckill.business.util;

import static cn.net.susan.constant.KeyConstant.SECKILL_PRODUCT_STOCK_PREFIX;

/**
 * Redis key工具类
 *
 */
public abstract class BusinessKeyUtil {

    private static final String USER_SECKILL_PRODUCT_PREFIX = "userSeckillProduct:";
    private static final String SECKILL_PRODUCT_OVER_PREFIX = "seckillProductOver:";

    private BusinessKeyUtil() {

    }

    /**
     * 获取秒杀商品
     * @param seckillProductId
     * @param userName
     * @return
     */
    public static String getUserSeckillProductKey(Long seckillProductId, String userName) {
        return String.format("%s%s_%s", USER_SECKILL_PRODUCT_PREFIX, seckillProductId, userName);
    }

    public static String getProductStockKey(Long seckillProductId) {
        return String.format("%s%s", SECKILL_PRODUCT_STOCK_PREFIX, seckillProductId);
    }

    public static String getSeckillProductRoutingKey(Long id) {
        return String.format("seckill_product_routing_key.%s", id);
    }

    public static String getCreateOrderRoutingKey(Long id) {
        return String.format("create_order_routing_key.%s", id);
    }

    public static String getProductOverKey(Long seckillProductId) {
        return String.format("%s%s", SECKILL_PRODUCT_OVER_PREFIX, seckillProductId);
    }
}
