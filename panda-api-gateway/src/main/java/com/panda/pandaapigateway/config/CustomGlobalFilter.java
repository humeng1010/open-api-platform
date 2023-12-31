package com.panda.pandaapigateway.config;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.URLUtil;
import com.panda.common.model.dto.userInterfaceInfo.InvokeCountRequest;
import com.panda.common.model.entity.InterfaceInfo;
import com.panda.common.model.entity.User;
import com.panda.pandaapigateway.service.PandaBackendClient;
import com.panda.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 全局过滤
 */
@Slf4j
public class CustomGlobalFilter implements GlobalFilter, Ordered {


    private final PandaBackendClient pandaBackendClient;

    public CustomGlobalFilter(PandaBackendClient pandaBackendClient) {
        this.pandaBackendClient = pandaBackendClient;
    }


    /**
     * 白名单
     */
    public static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1", "localhost");

    private static final String INTERFACE_HOST = "http://localhost:8123";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 创建线程池解决 openfegin 的同步问题
     */
    private ExecutorService executors = new ThreadPoolExecutor(16,
            20, 10, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(10), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("-------global filter start-------");
        ServerHttpRequest request = exchange.getRequest();
        String apiPath = request.getPath().value();
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
        body = URLUtil.decode(body);
        InterfaceInfo interfaceInfo = null;
        try {
            Future<InterfaceInfo> submit = executors.submit(() ->

                    pandaBackendClient.getInterfaceInfo(apiPath, method)
            );
            interfaceInfo = submit.get();
        } catch (Exception e) {
            log.error("getInterfaceInfo error", e);
        }
        if (interfaceInfo == null) {
            return handlerNoAuth(response);
        }

        if (Objects.equals(accessKey, "i am admin") && Objects.equals(sign, SignUtil.genSign(interfaceInfo.getRequestParams(), "online interface"))) {
            // 管理员发布接口 直接放行
            return chain.filter(exchange);
        }
        // 根据ak判断用户是否存在,查到sk,再判断加密后的sk是否一致
        User invokeUser = null;
        try {
            Future<User> submit = executors.submit(() -> pandaBackendClient.getInvokeUser(accessKey));
            invokeUser = submit.get();

        } catch (Exception e) {
            log.error("getInvokeUser error", e);
        }
        if (invokeUser == null) {
            return handlerNoAuth(response);
        }

        // 判断随机数,每次都是随机的10位数字
        if (nonce != null && nonce.length() != 10) {
            return handlerNoAuth(response);
        }

        // 判断时间戳 如果时间超过了5分钟就直接返回
        assert timestamp != null;
        // 发送请求的时间
        Date headerTime = new Date(Long.parseLong(timestamp));
        // 发送请求的时间+5分钟如果在当前时间之前就说明超过了5分钟,直接拦截
        if (LocalDateTimeUtil.of(headerTime).plusMinutes(5).isBefore(LocalDateTime.now())) {
            return handlerNoAuth(response);
        }
        // 解决 [请求重放] ,可以使用 timestamp + nonce 在有效的时间内判断随机数是否存在/重复
        // 重复返回false 不重复返回true 添加成功并且设置过期时间5分钟
        Boolean noRepeat = stringRedisTemplate
                .opsForValue()
                .setIfAbsent("panda:request:" + interfaceInfo.getId() + ":" + nonce,
                        "", 5, TimeUnit.MINUTES);

        // 如果存在随机值说明 可能是 请求重放 直接进行拦截
        if (Boolean.FALSE.equals(noRepeat)) {
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
                                            InvokeCountRequest invokeCountRequest = new InvokeCountRequest();
                                            invokeCountRequest.setInterfaceInfoId(interfaceInfoId);
                                            invokeCountRequest.setUserId(userId);
                                            executors.submit(() -> pandaBackendClient.invokeCount(invokeCountRequest));
                                            // pandaBackendClient.invokeCount(invokeCountRequest);
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
