package com.lzx.common.api.order.vo;


import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.domain.OrderInfo;

import java.io.Serializable;

/**
 * 订单详情，包含订单信息和商品信息
 *
 *
 */
public class OrderDetailVo implements Serializable {

    private GoodsVo goods;// 商品信息
    private OrderInfo order; // 订单信息

    public GoodsVo getGoods() {
        return goods;
    }

    public void setGoods(GoodsVo goods) {
        this.goods = goods;
    }

    public OrderInfo getOrder() {
        return order;
    }

    public void setOrder(OrderInfo order) {
        this.order = order;
    }
}
