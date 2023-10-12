package com.panda.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import com.panda.utils.SignUtil;

import java.util.HashMap;
import java.util.Map;

public class InvokeApiClient {
    private final String accessKey;

    private final String secretKey;

    public InvokeApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    private Map<String, String> getRequestHeaderMap(String body) {
        Map<String, String> header = new HashMap<>();
        header.put("accessKey", accessKey);
        // 密钥一定不能发送给后端
        // header.put("secretKey", secretKey);
        header.put("nonce", RandomUtil.randomNumbers(10));
        if (StrUtil.isBlank(body)) {
            body = "";
        }
        header.put("body", body);
        header.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 使用加密算法加密密钥
        header.put("sign", SignUtil.genSign(body, secretKey));

        return header;
    }

    public HttpResponse invokeApi(String method, String url, String requestParam) {

        return HttpRequest
                .of(url)
                .setMethod(Method.valueOf(method))
                .addHeaders(getRequestHeaderMap(requestParam))
                .body(requestParam)
                .execute();


    }
}
