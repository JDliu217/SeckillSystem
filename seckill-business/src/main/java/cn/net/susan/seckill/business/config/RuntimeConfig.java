package cn.net.susan.seckill.business.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * 项目运行时配置
 */
@MapperScan(basePackages = "cn.net.susan.seckill.business.mapper")
@Configuration
public class RuntimeConfig {
}
