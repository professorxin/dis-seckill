package com.lzx.common.api.user;

import com.lzx.common.api.user.vo.LoginVo;
import com.lzx.common.api.user.vo.RegisterVo;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.result.CodeMsg;

import javax.servlet.http.HttpServletResponse;

public interface UserServiceApi {

    String COOKIE_NAME = "token";

    String login(LoginVo loginVo);

    /**
     * 根据id查询秒杀用户信息
     * 对象级缓存
     *
     * @param id
     * @return
     */
    SeckillUser getById(Long id);


    boolean updatePassword(String token, long id, String updatePassword);


    /**
     * 注册
     * @param userModel
     * @return
     */
    CodeMsg register(RegisterVo userModel);


}
