package com.loyofo.redis.controller;

import com.loyofo.redis.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @author luojbin
 * @version 1.0
 * @create 2018/8/8 10:26
 */
@Controller
@RequestMapping("/hello")
public class HelloController {
    static {
        System.out.println("build HelloController");
    }

    @Autowired
    private HelloService helloService;

    @RequestMapping("/get")
    @ResponseBody
    public Map<String, Object> getOneArticle(){
        System.out.println("11");
        return helloService.getOneArticle();
    }
}
