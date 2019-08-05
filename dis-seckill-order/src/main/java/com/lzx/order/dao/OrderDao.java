package com.lzx.order.dao;


import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillOrder;

import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {


    @Select("SELECT * FROM seckill_order WHERE user_id = #{userId} AND goods_id = #{goodsId}")
    SeckillOrder getSeckillOrderByUserIdAndGoodsId(@Param("userId") Long id, @Param("goodsId") Long goodsId);


    @Insert("INSERT INTO order_info (user_id, goods_id, goods_name, goods_count, goods_price, order_channel, status, create_date)"
            + "VALUES (#{userId}, #{goodsId}, #{goodsName}, #{goodsCount}, #{goodsPrice}, #{orderChannel},#{status},#{createDate} )")
    @SelectKey(keyColumn = "id", keyProperty = "id", resultType = Long.class, before = false, statement = "SELECT last_insert_id()")
    long insert(OrderInfo orderInfo);


    @Insert("INSERT INTO seckill_order (user_id,order_id,goods_id) VALUE (#{userId},#{orderId},#{goodsId})")
    void insertSeckillOrder(SeckillOrder seckillOrder);


    @Select("select * from order_info where id = #{orderId}")
    OrderInfo getOrderById(@Param("orderId") long orderId);
}
