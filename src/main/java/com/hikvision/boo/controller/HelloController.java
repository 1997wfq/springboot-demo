package com.hikvision.boo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@ResponseBody
public class HelloController {
    @GetMapping("/hello")
    public String getHello(){
        return "hello springboot2";
    }
}
