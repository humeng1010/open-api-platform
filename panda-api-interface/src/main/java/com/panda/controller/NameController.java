package com.panda.controller;

import com.panda.model.entity.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getNameByGet(@RequestParam("name") String name) {

        return "GET:你的名字是" + name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam("name") String name) {
        return "POST:你的名字是" + name;
    }

    @PostMapping("/user")
    public String getNameByPost(@RequestBody User user, HttpServletRequest request) {
        // 统一迁移到网关Gateway做校验

        /*String accessKey = request.getHeader("accessKey");
        String nonce = request.getHeader("nonce");
        String timestamp = request.getHeader("timestamp");
        String sign = request.getHeader("sign");
        String body = request.getHeader("body");
        // TODO 查询数据库
        if (!Objects.equals(accessKey, "panda")) {
            throw new RuntimeException("无权限: 通行证无效");
        }
        if (nonce.length() > 5) {
            throw new RuntimeException("无权限: 随机数无效");
        }
        Date headerTime = new Date(Long.parseLong(timestamp));
        if (LocalDateTimeUtil.of(headerTime).plusMinutes(5).isAfter(LocalDateTime.now())) {
            throw new RuntimeException("无权限: 时间超时");
        }
        // TODO 实际是从数据库中取到secretKey
        String secretKey = "abcdefg";

        String serverSign = SignUtil.genSign(body, secretKey);
        if (!Objects.equals(serverSign, sign)) {
            throw new RuntimeException("无权限: 签名无效");
        }*/
        
        return "POST-request-body:你的名字是" + user.getName();
    }
}
