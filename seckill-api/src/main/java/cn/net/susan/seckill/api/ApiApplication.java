package cn.net.susan.seckill.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 秒杀系统API服务启动类
 *
 */
@SpringBootApplication(scanBasePackages = {"cn.net.susan"})
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
