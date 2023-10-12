package com.panda.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.panda.common.BaseResponse;
import com.panda.common.ResultUtils;
import com.panda.common.common.ErrorCode;
import com.panda.exception.BusinessException;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 返回一些随机的东西
 */
@RestController
@RequestMapping("/random")
public class RandomController {

    @GetMapping("/dog/image")
    public BaseResponse<String> getRandomDogImage() {
        HttpResponse res = HttpRequest.get("https://dog.ceo/api/breeds/image/random").execute();
        if (!res.isOk()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        DogResult result = JSONUtil.toBean(res.body(), DogResult.class);
        return ResultUtils.success(result.message);
    }

    @Data
    private static class DogResult {
        private String message;
        private String status;
    }

    @GetMapping("/joke")
    public String joke() {
        HttpResponse res = HttpRequest.get("https://api.vvhan.com/api/joke").execute();
        if (!res.isOk()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return res.body();
    }

}

