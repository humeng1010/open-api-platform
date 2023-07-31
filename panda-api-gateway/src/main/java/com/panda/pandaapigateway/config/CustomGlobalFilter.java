package com.panda.pandaapigateway.config;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.panda.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 全局过滤
 */
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    /**
     * 白名单
     */
    public static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1", "localhost");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("-------global filter start-------");
        ServerHttpRequest request = exchange.getRequest();
        log.info("请求唯一标识:{},请求路径:{},请求方法:{},请求参数:{},请求来源地址:{}",
                request.getId(), request.getPath().value(),
                request.getMethodValue(), request.getQueryParams(), request.getLocalAddress());

        String hostName = request.getLocalAddress().getHostName();

        ServerHttpResponse response = exchange.getResponse();
        if (!IP_WHITE_LIST.contains(hostName)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
        }

        // 鉴权
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        if (!Objects.equals(accessKey, "panda")) {
            return handlerNoAuth(response);
        }
        if (nonce != null && nonce.length() > 5) {
            return handlerNoAuth(response);
        }

        assert timestamp != null;
        Date headerTime = new Date(Long.parseLong(timestamp));
        if (LocalDateTimeUtil.of(headerTime).plusMinutes(5).isAfter(LocalDateTime.now())) {
            return handlerNoAuth(response);
        }

        String serverSign = SignUtil.genSign(body, "abcdefg");
        if (!Objects.equals(serverSign, sign)) {
            return handlerNoAuth(response);
        }

        // TODO 判断请求的模拟接口是否存在,使用远程调用


        // 请求转发调用模拟接口
        // Mono<Void> filter = chain.filter(exchange);
        // log.info("响应状态码:{}", response.getStatusCode());

        // log.info("-------global filter end---------");
        return handlerResponse(exchange, chain);


    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handlerResponse(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            ServerHttpResponse originalResponse = exchange.getResponse();
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();

            HttpStatus statusCode = originalResponse.getStatusCode();

            if (statusCode == HttpStatus.OK) {
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                        // TODO 调用成功 接口调用次数+1 剩余次数 -1 invokeCount


                                        byte[] content = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(content);
                                        DataBufferUtils.release(dataBuffer);// 释放掉内存
                                        String data = new String(content, StandardCharsets.UTF_8);// data
                                        // 打印日志
                                        log.info("响应结果:" + data);
                                        return bufferFactory.wrap(content);
                                    }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };

                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 降级处理返回数据
            return chain.filter(exchange);
        } catch (Exception e) {
            log.error("网关处理响应异常\n" + e);
            return chain.filter(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }


    public Mono<Void> handlerNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    public Mono<Void> handlerNoError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
}
