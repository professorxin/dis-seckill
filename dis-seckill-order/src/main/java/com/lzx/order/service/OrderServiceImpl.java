package com.lzx.order.service;

import com.lzx.common.api.cache.RedisServiceApi;
import com.lzx.common.api.cache.vo.OrderKeyPrefix;
import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.api.order.OrderServiceApi;
import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillOrder;
import com.lzx.common.domain.SeckillUser;
import com.lzx.order.dao.OrderDao;

import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderServiceImpl implements OrderServiceApi {

    @Autowired
    private OrderDao orderDao;

    @Reference
    private RedisServiceApi redisServiceApi;

    public SeckillOrder getSeckillOrderByUserIdAndGoodsId(Long id, Long goodsId) {
        SeckillOrder seckillOrder = redisServiceApi.get(OrderKeyPrefix.getSeckillOrderByUidGid, "" + id + "_" + goodsId,
                SeckillOrder.class);
        return  seckillOrder;
    }


    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());

        //订单信息插入order_info表中
        orderDao.insert(orderInfo);

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setGoodsId(goods.getId());
        seckillOrder.setUserId(user.getId());

        //秒杀订单信息插入seckill_order表
        orderDao.insertSeckillOrder(seckillOrder);

        redisServiceApi.set(OrderKeyPrefix.getSeckillOrderByUidGid,
                "" + user.getId() + "_" + goods.getId(), seckillOrder);
        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
}
