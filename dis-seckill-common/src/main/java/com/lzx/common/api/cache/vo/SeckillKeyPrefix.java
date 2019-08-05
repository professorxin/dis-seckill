package com.lzx.common.api.cache.vo;

public class SeckillKeyPrefix extends BaseKeyPrefix {


    public SeckillKeyPrefix(String prefix) {
        super(prefix);
    }

    public SeckillKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static SeckillKeyPrefix isGoodsOver = new SeckillKeyPrefix("isGoodsOver");

    public static SeckillKeyPrefix seckillPath = new SeckillKeyPrefix(60, "seckillPath");

    public static SeckillKeyPrefix seckillVerifyCode = new SeckillKeyPrefix(300, "seckillVerifyCode");


}
