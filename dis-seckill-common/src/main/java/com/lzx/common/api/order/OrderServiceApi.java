package com.lzx.common.api.order;

import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillOrder;
import com.lzx.common.domain.SeckillUser;

import java.util.Date;

public interface OrderServiceApi {

    SeckillOrder getSeckillOrderByUserIdAndGoodsId(Long id, Long goodsId);


    OrderInfo createOrder(SeckillUser user, GoodsVo goods);

    OrderInfo getOrderById(long orderId);
}
