package com.sso.login.controller;

import com.sso.login.pojo.User;
import com.sso.login.utils.LogCacheUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Set<User> dbUser;
    static{
        dbUser = new HashSet<>();
        dbUser.add(new User(1,"airco","123456","男", "182xxxxxxxx",
                "大三", "班级", 1, "JavaWeb",
                "", "www.aiecoblog.com", "", ""));
        dbUser.add(new User(2,"xiaoliu","000000","女", "135xxxxxxxx",
                "大三", "班级", 2, "Database",
                "", "www.liuboyblog.com", "", ""));
    }

    //登录跳转
    @PostMapping
    public String doLogin(User user, HttpSession session, HttpServletResponse response){
        String target = (String) session.getAttribute("target");
        Optional<User> first = dbUser.stream().filter(dbUser -> dbUser.getUsername().equals(user.getUsername()) &&
                dbUser.getPassword().equals(user.getPassword()))
                .findFirst();
        if(first.isPresent()){
            String token = UUID.randomUUID().toString();
            Cookie cookie = new Cookie("TOKEN",token);
            cookie.setDomain("127.0.0.1");
            response.addCookie(cookie);
            LogCacheUtil.loginUser.put(token, first.get());
        }else{
            session.setAttribute("msg","username or password is wrong.");
            return "login";
        }
        return "redirect:" + target;
    }

    //已登录直接跳转
    @GetMapping("info")
    @ResponseBody
    public ResponseEntity<User> getUserInfo(String token){
        if(!StringUtils.isEmpty(token)){
            User user = LogCacheUtil.loginUser.get(token);
            return ResponseEntity.ok(user);
        }else{
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    //退出功能让cookie过期11/7
    @GetMapping("/loginOut")
    public String loginOut(@CookieValue(value = "TOKEN")Cookie cookie,HttpServletResponse response,String target){
        cookie.setMaxAge(0);
        LogCacheUtil.loginUser.remove(cookie.getValue());
        return "redirect:"+target;
    }
}
