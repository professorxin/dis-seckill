package com.lzx.gateway.config.access;

import com.alibaba.fastjson.JSON;
import com.lzx.common.api.cache.RedisServiceApi;
import com.lzx.common.api.cache.vo.AccessKeyPrefix;
import com.lzx.common.api.user.UserServiceApi;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.result.CodeMsg;
import com.lzx.common.result.Result;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {


    @Reference
    private UserServiceApi userServiceApi;

    @Reference
    private RedisServiceApi redisServiceApi;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            SeckillUser user = this.getUser(request, response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null) {
                return true;
            }
            // 获取注解的元素值
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxAccessCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin) {
                if (user == null) {
                    this.render(response, CodeMsg.SESSION_ERROR);
                }
                key += "_" + user.getId();
            }
            // 设置过期时间
            AccessKeyPrefix accessKeyPrefix = AccessKeyPrefix.withExpire(seconds);
            // 在redis中存储的访问次数的key为请求的URI
            Integer count = redisServiceApi.get(accessKeyPrefix, key, Integer.class);
            // 第一次重复点击秒杀
            if (count == null) {
                redisServiceApi.set(accessKeyPrefix, key, 1);
                // 点击次数为未达最大值
            } else if (count < maxCount) {
                redisServiceApi.incr(accessKeyPrefix, key);
                // 点击次数已满
            } else {
                this.render(response, CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }

        }
        return true;
    }

    /**
     * 点击次数已满后，向客户端反馈一个“频繁请求”提示信息
     *
     * @param response
     * @param cm
     * @throws Exception
     */
    private void render(HttpServletResponse response, CodeMsg cm) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }


    private SeckillUser getUser(HttpServletRequest request, HttpServletResponse response) {

        // 从请求中获取token
        String paramToken = request.getParameter(UserServiceApi.COOKIE_NAME);
        String cookieToken = getCookieValue(request, UserServiceApi.COOKIE_NAME);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        return userServiceApi.getByToken(response, token);
    }


    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
