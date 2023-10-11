package com.panda.pandaapigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CustomGlobalFilter.class)
public class GateWayConfig {
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        // 编程式断言跳转
        return builder.routes()
                .route("route1",
                        r -> r.path("/l").uri("https://huya.com/"))
                .route("route2",
                        r -> r.path("/g").uri("https://huya.com/"))
                .build();
    }
}