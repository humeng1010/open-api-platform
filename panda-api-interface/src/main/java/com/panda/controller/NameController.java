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
        return "POST-request-body:你的名字是" + user.getName();
    }
}
