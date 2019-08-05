package com.lzx.common.api.goods;

import com.lzx.common.api.goods.vo.GoodsVo;

import java.util.List;

public interface GoodsServiceApi {

    List<GoodsVo> listGoodsVo();

    GoodsVo getGoodsVoById(Long goodsId);

    boolean reduceStock(GoodsVo goods);
}
