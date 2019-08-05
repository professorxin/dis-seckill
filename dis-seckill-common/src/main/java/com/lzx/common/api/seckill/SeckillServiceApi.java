package com.lzx.common.api.seckill;

import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillUser;

import java.awt.image.BufferedImage;

public interface SeckillServiceApi {


    OrderInfo seckill(SeckillUser user, GoodsVo goods) ;



    long getSeckillResult(Long userId, Long goodsId) ;


    /**
     * 验证路径是否正确
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    boolean checkPath(SeckillUser user, long goodsId, String path) ;

    /**
     * 创建秒杀地址，并存储在redis中
     *
     * @param user
     * @param goodsId
     * @return
     */
    String createSeckillPath(SeckillUser user, long goodsId) ;

    BufferedImage createVerifyCode(SeckillUser user, long goodsId) ;
}
