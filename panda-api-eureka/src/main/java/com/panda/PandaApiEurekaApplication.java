package com.panda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author humeng
 */
@SpringBootApplication
@EnableEurekaServer // 开启Eureka服务
public class PandaApiEurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PandaApiEurekaApplication.class, args);
    }
}
