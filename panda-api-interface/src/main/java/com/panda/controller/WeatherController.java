package com.panda.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.panda.common.common.ErrorCode;
import com.panda.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/weather")
@Slf4j
public class WeatherController {
    @GetMapping
    public ResponseEntity<String> getWeather(@RequestParam("city") String city) {
        log.info("城市:{}", city);
        HttpResponse response = HttpRequest.get("https://api.vvhan.com/api/weather?city=" + city).execute();
        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return ResponseEntity.ok(response.body());
    }
}
