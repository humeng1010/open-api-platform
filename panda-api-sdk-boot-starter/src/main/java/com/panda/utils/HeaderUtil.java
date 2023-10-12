package com.panda.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求头部的信息
 *
 * @author humeng
 */
public class HeaderUtil {
    public static Map<String, String> getRequestHeaderMap(String accessKey, String secretKey, String body) {
        Map<String, String> header = new HashMap<>(16);
        // 密钥一定不能发送给后端
        header.put("accessKey", accessKey);
        header.put("nonce", RandomUtil.randomNumbers(10));
        if (StrUtil.isBlank(body)) {
            body = "";
        }
        header.put("body", URLUtil.encode(body));
        header.put("timestamp", String.valueOf(System.currentTimeMillis()));
        // 使用加密算法加密密钥
        header.put("sign", SignUtil.genSign(body, secretKey));

        return header;
    }
}
