package com.bzy.takeaway;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * BZY外卖系统 启动类
 */
@SpringBootApplication
@MapperScan("com.bzy.takeaway.mapper")
@ServletComponentScan
@EnableAsync
public class BzyTakeawayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BzyTakeawayApplication.class, args);
        System.out.println("========================================");
        System.out.println("  BZY外卖系统 启动成功!");
        System.out.println("  接口地址: http://localhost:8080/api");
        System.out.println("  前端地址: http://localhost:8080");
        System.out.println("========================================");
    }
}
