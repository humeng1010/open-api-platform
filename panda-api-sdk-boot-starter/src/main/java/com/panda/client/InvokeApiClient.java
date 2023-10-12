package com.panda.client;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;

import java.nio.charset.StandardCharsets;

import static com.panda.utils.HeaderUtil.getRequestHeaderMap;

/**
 * 通过method url requestParams调用接口
 *
 * @author humeng
 */
public class InvokeApiClient {
    private final String accessKey;

    private final String secretKey;

    public InvokeApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }


    public HttpResponse invokeApi(String method, String url, String requestParam) {

        return HttpRequest
                .of(url)
                .setMethod(Method.valueOf(method))
                .addHeaders(getRequestHeaderMap(accessKey, secretKey, requestParam))
                .charset(StandardCharsets.UTF_8)
                .body(requestParam)
                .execute();
    }
}
