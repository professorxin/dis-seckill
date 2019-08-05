package com.lzx.common.api.cache.vo;

public class OrderKeyPrefix extends BaseKeyPrefix {

    public OrderKeyPrefix(String prefix) {
        super(prefix);
    }

    public static OrderKeyPrefix getSeckillOrderByUidGid = new OrderKeyPrefix("getSeckillOrderByUidGid");
}
