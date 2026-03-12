package cn.net.susan.seckill.business.config;

import cn.net.susan.util.SnowFlakeIdWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式ID配置
 *
 */
@Configuration
public class IdGenerateConfig {

    @Bean
    public SnowFlakeIdWorker snowFlakeIdWorker() {
        return new SnowFlakeIdWorker(0, 0);
    }
}
