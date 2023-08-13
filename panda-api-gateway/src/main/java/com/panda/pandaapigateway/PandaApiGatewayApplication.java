package com.panda.pandaapigateway;

import com.panda.NacosService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
@EnableDubbo
@EnableDiscoveryClient
public class PandaApiGatewayApplication {

    @DubboReference
    private NacosService nacosService;

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(PandaApiGatewayApplication.class, args);
        PandaApiGatewayApplication bean = applicationContext.getBean(PandaApiGatewayApplication.class);
        bean.sayHello();
    }

    public void sayHello() {
        nacosService.sayHello();
    }


}
