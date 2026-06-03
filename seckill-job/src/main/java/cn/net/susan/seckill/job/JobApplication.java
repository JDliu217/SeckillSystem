package cn.net.susan.seckill.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 秒杀系统job服务启动类
 *
 */
@SpringBootApplication(scanBasePackages = {"cn.net.susan"})
public class JobApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobApplication.class, args);
    }
}
