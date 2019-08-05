package com.lzx.gateway.config.access;

import com.lzx.common.domain.SeckillUser;


public class UserContext {

    public static ThreadLocal<SeckillUser> userHolder = new ThreadLocal<>();

    public static void setUser(SeckillUser user){
        userHolder.set(user);
    }

    public static SeckillUser getUser(){
        return userHolder.get();
    }
}
