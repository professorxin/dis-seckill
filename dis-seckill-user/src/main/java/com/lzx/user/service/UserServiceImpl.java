package com.lzx.user.service;

import com.lzx.common.api.cache.RedisLockApi;
import com.lzx.common.api.cache.RedisServiceApi;
import com.lzx.common.api.cache.vo.SeckillUserKeyPrefix;
import com.lzx.common.api.user.UserServiceApi;
import com.lzx.common.api.user.vo.LoginVo;
import com.lzx.common.api.user.vo.RegisterVo;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.exception.GlobleException;
import com.lzx.common.result.CodeMsg;
import com.lzx.common.util.MD5Util;
import com.lzx.common.util.UUIDUtil;

import com.lzx.user.dao.SeckillUserDao;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Service
public class UserServiceImpl implements UserServiceApi {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    public static final String COOKIE_NAME = "token";

    @Autowired
    private SeckillUserDao seckillUserDao;

    @Reference
    private RedisServiceApi redisServiceApi;

    @Reference
    private RedisLockApi redisLockApi;

    public String login(LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobleException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //验证手机号是否存在
        SeckillUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobleException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码是否相等
        String dbPassword = user.getPassword();
        String dbSalt = user.getSalt();
        String calcPassWord = MD5Util.formPassToDbPass(password, dbSalt);
        //log.info("数据库密码为{}，校验密码为{}", dbPassword, calcPassWord);
        if (!calcPassWord.equals(dbPassword)) {
            throw new GlobleException(CodeMsg.PASSWORD_ERROR);
        }
        String token = UUIDUtil.uuid();
        //addCookie(response, token, user);
        redisServiceApi.set(SeckillUserKeyPrefix.token, token, user);
        return token;
    }

    /**
     * 根据id查询秒杀用户信息
     * 对象级缓存
     *
     * @param id
     * @return
     */
    public SeckillUser getById(Long id) {
        //从redis缓存中获取用户数据缓存
        SeckillUser seckillUser = redisServiceApi.get(SeckillUserKeyPrefix.getSeckillUserById,
                "" + id, SeckillUser.class);
        if (seckillUser != null) {
            return seckillUser;
        }
        //缓存中没有数据就从数据库中查，并放入缓存
        seckillUser = seckillUserDao.getById(id);
        if (seckillUser != null) {
            redisServiceApi.set(SeckillUserKeyPrefix.getSeckillUserById, "" + id, seckillUser);
        }
        return seckillUser;
    }


    public boolean updatePassword(String token, long id, String updatePassword) {
        //调用service层根据id查询用户数据，可能走缓存或者数据库。直接调用dao层的话就直接走数据库了
        SeckillUser seckillUser = getById(id);
        if (seckillUser == null) {
            throw new GlobleException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //用户数据存在，更新数据
        SeckillUser updateUser = new SeckillUser();
        updateUser.setId(id);
        updateUser.setPassword(MD5Util.inputPassToDbPass(updatePassword, seckillUser.getSalt()));
        seckillUserDao.updatePassword(updateUser);
        //删除缓存中的旧数据
        redisServiceApi.delete(SeckillUserKeyPrefix.getSeckillUserById, "" + id);
        //根据token查找用户信息的旧数据不能直接删除，直接删除会导致程序无法获取用户信息了，改为重新设置值
        seckillUser.setPassword(updateUser.getPassword());
        redisServiceApi.set(SeckillUserKeyPrefix.token, token, seckillUser);
        return true;
    }

    @Override
    public CodeMsg register(RegisterVo userModel) {
        //加锁
        String uniqueValue = UUIDUtil.uuid() + "-" + Thread.currentThread().getId();
        String lockKey = "redis-lock" + userModel.getPhone();
        boolean lock = redisLockApi.lock(lockKey, uniqueValue, 60 * 1000);
        if (!lock) {
            return CodeMsg.WAIT_REGISTER_DONE;
        }
        log.debug("注册接口加锁成功");

        //检查用户是否注册
        SeckillUser user = this.getById(userModel.getPhone());
        if (user != null) {
            redisLockApi.unlock(lockKey, uniqueValue);
            return CodeMsg.USER_EXIST;
        }

        //生成秒杀对象
        SeckillUser seckillUser = new SeckillUser();
        seckillUser.setId(userModel.getPhone());
        seckillUser.setNickname(userModel.getNickname());
        seckillUser.setHead(userModel.getHead());
        seckillUser.setSalt(MD5Util.salt);

        String dbPass = MD5Util.formPassToDbPass(userModel.getPassword(), MD5Util.salt);
        seckillUser.setPassword(dbPass);

        Date date = new Date(System.currentTimeMillis());
        seckillUser.setRegisterDate(date);

        //写入数据库
        long id = seckillUserDao.insertUser(seckillUser);

        boolean unlock = redisLockApi.unlock(lockKey, uniqueValue);
        if (!unlock) {
            return CodeMsg.REGISTER_FAIL;
        }
        log.debug("注册接口解锁成功");

        //用户注册成功
        if (id > 0) {
            return CodeMsg.SUCCESS;
        }

        //用户注册失败
        return CodeMsg.REGISTER_FAIL;

    }


    private void addCookie(HttpServletResponse response, String token, SeckillUser user) {
        redisServiceApi.set(SeckillUserKeyPrefix.token, token, user);
        Cookie cookie = new Cookie(COOKIE_NAME, token);
        cookie.setPath("/");
        cookie.setMaxAge(SeckillUserKeyPrefix.TOKEN_EXPIRE);
        response.addCookie(cookie);
    }
}
