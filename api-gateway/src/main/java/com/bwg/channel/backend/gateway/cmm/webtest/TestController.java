package com.bwg.channel.backend.gateway.cmm.webtest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/te")
@Controller
public class TestController {
    @GetMapping("/st.do")
    public String testLoginPage() {
        return "login"; // templates/login.html
    }

    @PostMapping("/home")
    public String testHomePage() {
        return "home"; // templates/login.html
    }
}
