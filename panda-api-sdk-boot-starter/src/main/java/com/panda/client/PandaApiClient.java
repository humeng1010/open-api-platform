package com.panda.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import lombok.extern.slf4j.Slf4j;

import static com.panda.utils.HeaderUtil.getRequestHeaderMap;

/**
 * 调用第三方接口的客户端 具体的接口 每新增一个接口就需要更新一下客户端工具
 *
 * @author humeng
 */
@Slf4j
public class PandaApiClient {

    private final String accessKey;

    private final String secretKey;

    public static final String GATEWAY_HOST = "http://localhost:8090";


    public PandaApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getNameByGet(String name) {
        HttpResponse response = HttpRequest.get(GATEWAY_HOST + "/api/name")
                .addHeaders(getRequestHeaderMap(accessKey, secretKey, name))
                .execute();
        log.info("结果:{}", response);
        return response.body();
    }


    public String joke() {
        HttpResponse httpResponse = HttpRequest.get(GATEWAY_HOST + "/api/random/joke")
                .addHeaders(getRequestHeaderMap(accessKey, secretKey, ""))
                .execute();
        log.info("状态码:{};响应体:{}", httpResponse.getStatus(), httpResponse.body());
        return httpResponse.body();
    }
}
