package com.lzx.gateway.user;

import com.lzx.common.api.seckill.SeckillServiceApi;
import com.lzx.common.api.user.UserServiceApi;
import com.lzx.common.api.user.vo.LoginVo;
import com.lzx.common.result.Result;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Reference
    private UserServiceApi userServiceApi;

    @RequestMapping("/to_login")
    public String toLogin() {
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
        String token = userServiceApi.login(response, loginVo);
        return Result.success(token);
    }

}
