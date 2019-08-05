package com.lzx.common.api.cache.vo;

import java.io.Serializable;

public class GoodsKeyPrefix extends BaseKeyPrefix implements Serializable {

    public GoodsKeyPrefix(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public GoodsKeyPrefix(String prefix) {
        super(prefix);
    }

    public static GoodsKeyPrefix goodsListKeyPrefix = new GoodsKeyPrefix(60, "goodsList");

    public static GoodsKeyPrefix goodsDetailKeyPrefix = new GoodsKeyPrefix(60, "goodsDetail");

    public static GoodsKeyPrefix seckillGoodsStock = new GoodsKeyPrefix("seckillGoodsStock");

}
