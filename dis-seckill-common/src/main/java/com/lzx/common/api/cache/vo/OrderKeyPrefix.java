package com.lzx.common.api.cache.vo;

import java.io.Serializable;

public class OrderKeyPrefix extends BaseKeyPrefix implements Serializable {

    public OrderKeyPrefix(String prefix) {
        super(prefix);
    }

    public static OrderKeyPrefix getSeckillOrderByUidGid = new OrderKeyPrefix("getSeckillOrderByUidGid");
}
