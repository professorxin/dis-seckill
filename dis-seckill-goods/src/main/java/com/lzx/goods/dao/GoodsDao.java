package com.lzx.goods.dao;

import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.domain.SeckillGoods;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    @Select("SELECT g.*,sg.seckill_price,sg.stock_count,sg.start_date,sg.end_date FROM seckill_goods sg " +
            "LEFT JOIN goods g ON sg.goods_id = g.id")
    List<GoodsVo> listGoodsVo();


    @Select("SELECT g.*,sg.seckill_price,sg.stock_count,sg.start_date,sg.end_date FROM seckill_goods sg " +
            "LEFT JOIN goods g ON sg.goods_id = g.id where sg.goods_id = #{goodsId}")
    GoodsVo getGoodsVoById(@Param("goodsId") Long goodsId);


    @Update("UPDATE seckill_goods SET stock_count = stock_count - 1 WHERE goods_id =#{goodsId} " +
            "AND stock_count > 0")
    int reduceStock(SeckillGoods seckillGoods);
}
