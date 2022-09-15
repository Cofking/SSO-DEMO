package com.ssodemo.sso.controller;

import com.sun.deploy.net.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class ssocontroller {

    @Autowired
    RedisTemplate redisTemplate;

    @PostMapping("/doLogin")
    public String doLogin(Model model, @RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("url") String url, HttpServletResponse response) {
        if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
            //登录成功
            RedisSerializer stringSerializer = new StringRedisSerializer();
            String uuid = UUID.randomUUID().toString().replace("-", "");
            redisTemplate.setKeySerializer(stringSerializer);
            redisTemplate.setValueSerializer(stringSerializer);
            redisTemplate.opsForValue().set("login:" + uuid, username, 30, TimeUnit.MINUTES);
            Cookie sso_token = new Cookie("sso_token", uuid);
            response.addCookie(sso_token);
            return "redirect:" + url + "?token=" + uuid;
        }
        model.addAttribute("url", url);
        return "login";
    }

    @GetMapping("/login")
    public String hello(@RequestParam("url") String url, Model model,
                        @CookieValue(value = "sso_token", required = false) String sso_token) {
        if (!StringUtils.isEmpty(sso_token)) {
            Object o = redisTemplate.opsForValue().get("login:" + sso_token);
            if (o != null) {
                //已经登录过
                return "redirect:" + url + "?token=" + sso_token;
            }
        }
        model.addAttribute("url", url);
        return "login";
    }

    @ResponseBody
    @GetMapping("/userInfo")
    public String userInfo(@RequestParam("token") String token) {
        return (String) redisTemplate.opsForValue().get("login:" + token);
    }

    /**
     * 退出登录
     * @param url
     * @return
     */
    @GetMapping("/logout")
    public String logout(@RequestParam("url") String url, @CookieValue(value = "sso_token", required = false) String sso_token,HttpServletResponse response) {
        redisTemplate.delete("login:"+sso_token);
        response.addCookie(new Cookie("sso_token", "1"));
        return "redirect:" + url+"?token=1";
    }


}
