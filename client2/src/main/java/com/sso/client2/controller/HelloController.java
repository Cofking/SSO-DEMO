package com.sso.client2.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.HttpSession;

@Controller
public class HelloController {

    @Value("${sso.server.url}")
     String ssoServerUrl;

    @GetMapping("/boss")
    public String boss(Model model, HttpSession session ,
                            @RequestParam(value = "token",required = false)String token){
        if (!StringUtils.isEmpty(token)) {// 去redis 验证用户是否有效
            //登录成功
            //去sso中获取当前真正用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://sso.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            if (!StringUtils.isEmpty(body)) {
                session.setAttribute("loginUser", body);
            }else {
                //判断redis中不存在toke的时候 删除一次本地 session
                session.removeAttribute("loginUser");
            }
        }
        Object loginUser = session.getAttribute("loginUser");
        if(loginUser==null){
            //没登录 跳转登录服务器登录
            return "redirect:"+ssoServerUrl+"?url=http://client2.com:8082/boss";
        }
        return "boss";
    }
}
