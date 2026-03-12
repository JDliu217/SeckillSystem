package cn.net.susan.seckill.business.config;

import cn.net.susan.seckill.business.config.properties.OrderApiProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 业务配置类
 *
 */
@Data
@Component
@Slf4j
@ConfigurationProperties(prefix = "seckill")
public class BusinessConfig {

    /**
     * 订单API接口配置
     */
    private OrderApiProperties orderApiProperties = new OrderApiProperties();

}
