package com.panda.pandaapigateway.config;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.panda.common.model.entity.InterfaceInfo;
import com.panda.common.model.entity.User;
import com.panda.common.service.InnerInterfaceInfoService;
import com.panda.common.service.InnerUserInterfaceInfoService;
import com.panda.common.service.InnerUserService;
import com.panda.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
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

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    /**
     * 白名单
     */
    public static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1", "localhost");

    private static final String INTERFACE_HOST = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("-------global filter start-------");
        ServerHttpRequest request = exchange.getRequest();
        String path = INTERFACE_HOST + request.getPath().value();
        String method = request.getMethodValue();

        log.info("请求唯一标识:{},请求路径:{},请求方法:{},请求参数:{},请求来源地址:{}",
                request.getId(), path,
                method, request.getQueryParams(), request.getLocalAddress());

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
        // 根据ak判断用户是否存在,查到sk,再判断加密后的sk是否一致
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);

        } catch (Exception e) {
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handlerNoAuth(response);
        }

        // 判断随机数
        if (nonce != null && nonce.length() > 5) {
            return handlerNoAuth(response);
        }

        // 判断时间
        assert timestamp != null;
        Date headerTime = new Date(Long.parseLong(timestamp));
        if (LocalDateTimeUtil.of(headerTime).plusMinutes(5).isBefore(LocalDateTime.now())) {
            return handlerNoAuth(response);
        }

        // 从数据库中获取用户的密钥
        String secretKey = invokeUser.getSecretKey();

        // 把body和密钥加密为签名
        String serverSign = SignUtil.genSign(body, secretKey);
        // 如果和用户传递的签名不一致,拦截
        if (!Objects.equals(serverSign, sign)) {
            return handlerNoAuth(response);
        }
        // 获取接口信息
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
        }
        if (interfaceInfo == null) {
            return handlerNoAuth(response);
        }

        // todo 判断该用户是否还有该接口的调用次数


        // 请求转发+响应日志
        return handlerResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());


    }

    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handlerResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceInfoId, long userId) {
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
                                        // 调用成功 接口调用次数+1 剩余次数 -1 invokeCount
                                        try {
                                            innerUserInterfaceInfoService.invokeCount(interfaceInfoId, userId);
                                        } catch (Exception e) {
                                            log.error("invokeCount error", e);
                                        }

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
