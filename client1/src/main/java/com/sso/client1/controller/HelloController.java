package com.sso.client1.controller;


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

    @GetMapping("/employees")
    public String employees(Model model, HttpSession session,
                            @RequestParam(value = "token", required = false) String token) {

        if (!StringUtils.isEmpty(token)) {// 去redis 验证用户是否有效
            //登录成功
            //去sso中获取当前真正用户信息
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> forEntity = restTemplate.getForEntity("http://sso.com:8080/userInfo?token=" + token, String.class);
            String body = forEntity.getBody();
            if (!StringUtils.isEmpty(body)) {
                //令牌有效 创建局部会话
                session.setAttribute("loginUser", body);
            }else {
                //令牌无效 删除局部会话
                //判断redis中不存在toke的时候 删除一次本地 session
                session.removeAttribute("loginUser");
            }
        }
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser == null) {
            //没登录 跳转登录服务器登录
            return "redirect:" + ssoServerUrl + "?url=http://client1.com:8081/employees";
        }
        return "list";
    }

    @GetMapping("/logout")
    public String logout() {
        return "redirect:" + "http://sso.com:8080/logout" + "?url=http://client1.com:8081/employees";
    }
}
