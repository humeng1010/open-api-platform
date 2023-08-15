package com.panda.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.panda.model.entity.User;
import com.panda.utils.SignUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 调用第三方接口的客户端
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
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        String result = HttpUtil.get(GATEWAY_HOST + "/api/name", params);
        log.info("结果:{}", result);
        return result;
    }

    public String getNameByPost(String name) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("name", "张三");
        String result = HttpUtil.post(GATEWAY_HOST + "/api/name", params);
        log.info("结果:{}", result);
        return result;
    }

    private Map<String, String> getRequestHeaderMap(String body) {
        Map<String, String> header = new HashMap<>();
        header.put("accessKey", accessKey);
        // 密钥一定不能发送给后端
        // header.put("secretKey", secretKey);
        header.put("nonce", RandomUtil.randomNumbers(5));
        header.put("body", body);
        header.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 使用加密算法加密密钥
        header.put("sign", SignUtil.genSign(body, secretKey));

        return header;
    }


    public String getNameByPost(User user) {
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + "/api/name/user")
                .charset(StandardCharsets.UTF_8)
                .body(json)
                .addHeaders(getRequestHeaderMap(json))
                .execute();
        log.info("状态码:{};响应体:{}", httpResponse.getStatus(), httpResponse.body());
        return httpResponse.body();
    }
}
