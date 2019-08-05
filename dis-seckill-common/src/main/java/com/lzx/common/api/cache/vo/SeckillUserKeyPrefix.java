package com.lzx.common.api.cache.vo;

import java.io.Serializable;

public class SeckillUserKeyPrefix extends BaseKeyPrefix implements Serializable {

    public static final int TOKEN_EXPIRE = 3600 * 24 * 2;

    public SeckillUserKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public SeckillUserKeyPrefix(String prefix){
        super(prefix);
    }

    public static SeckillUserKeyPrefix token = new SeckillUserKeyPrefix(TOKEN_EXPIRE, "token");

    public static SeckillUserKeyPrefix getSeckillUserById = new SeckillUserKeyPrefix("id");

}
