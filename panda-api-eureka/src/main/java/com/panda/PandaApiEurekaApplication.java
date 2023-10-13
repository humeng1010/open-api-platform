package com.panda;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * eureka 注册中心
 *
 * @author humeng
 */
@SpringBootApplication
@EnableEurekaServer
public class PandaApiEurekaApplication {
    public static void main(String[] args) {
        SpringApplication.run(PandaApiEurekaApplication.class, args);
    }
}
