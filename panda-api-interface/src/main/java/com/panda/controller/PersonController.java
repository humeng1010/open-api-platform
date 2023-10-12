package com.panda.controller;

import cn.hutool.core.date.DateUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author humeng
 */
@RestController
@RequestMapping("/person")
public class PersonController {
    /**
     * 通过生日计算星座
     *
     * @param m 月
     * @param d 日
     * @return 星座
     */
    @GetMapping("/zodiac")
    public String getPersonZodiacByDayOfMonth(@RequestParam("m") Integer m, @RequestParam("d") Integer d) {
        return DateUtil.getZodiac(m - 1, d);
    }

    /**
     * 通过年份得到生肖
     *
     * @param year 1900年之后的
     * @return 生肖
     */
    @GetMapping("/chineseZodiac")
    public String getPersonChineseZodiacByYear(@RequestParam("year") Integer year) {
        return DateUtil.getChineseZodiac(year);
    }
}
