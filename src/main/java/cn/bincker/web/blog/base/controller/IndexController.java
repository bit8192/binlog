package cn.bincker.web.blog.base.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class IndexController {
    @GetMapping
    public String index(){
        return "index";
    }
}
