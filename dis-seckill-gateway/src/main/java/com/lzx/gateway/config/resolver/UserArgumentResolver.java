package com.lzx.gateway.config.resolver;


import com.lzx.common.api.user.UserServiceApi;
import com.lzx.common.domain.SeckillUser;
import com.lzx.gateway.config.access.UserContext;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 自定义参数解析器
 * 将请求中的参数进行处理，设置到SeckillUser中
 */
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Reference
    private UserServiceApi seckillUserService;

    /**
     * 当请求参数中含有SeckillUser时，Controller中该对象是由下面的resolveArgument方法获得
     *
     * @param methodParameter
     * @return
     */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();
        return clazz == SeckillUser.class;
    }

    /**
     * 解析request和response，返回SeckillUser对象
     *
     * @param methodParameter
     * @param modelAndViewContainer
     * @param nativeWebRequest
     * @param webDataBinderFactory
     * @return
     * @throws Exception
     */
    @Override
    public Object resolveArgument(MethodParameter methodParameter,
                                  ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest,
                                  WebDataBinderFactory webDataBinderFactory) throws Exception {
/*
        //获取请求和响应对象
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);

        String paramToken = request.getParameter(SeckillUserService.COOKIE_NAME);
        String cookieToken = getCookieValue(request, SeckillUserService.COOKIE_NAME);
        //System.out.println(paramToken+"--------------"+cookieToken);
        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken) ? cookieToken : paramToken;
        SeckillUser seckillUser = seckillUserService.getByToken(response, token);
        return seckillUser;*/
        return UserContext.getUser();
    }

    /**
     * 根据cookie名获取相应的cookie值
     *
     * @param request
     * @param cookieName
     * @return
     */
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length <= 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
