package com.lzx.mq.receiver;


import com.lzx.common.api.goods.GoodsServiceApi;
import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.api.mq.vo.SeckillMessage;
import com.lzx.common.api.order.OrderServiceApi;
import com.lzx.common.api.seckill.SeckillServiceApi;
import com.lzx.common.domain.SeckillOrder;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.util.JsonUtil;
import com.lzx.mq.config.MQConfig;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {


    @Reference
    private GoodsServiceApi goodsServiceApi;

    @Reference
    private OrderServiceApi orderServiceApi;

    @Reference
    private SeckillServiceApi seckillServiceApi;

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);


    @RabbitListener(queues = MQConfig.SECKILL_QUEUE)
    public void receiveSeckillMsg(String message) {
        log.info("receive seckill_queue message:" + message);
        SeckillMessage seckillMessage = JsonUtil.stringToBean(message, SeckillMessage.class);
        SeckillUser user = seckillMessage.getUser();
        Long goodsId = seckillMessage.getGoodsId();
        //判断是否还有秒杀库存
        GoodsVo goods = goodsServiceApi.getGoodsVoById(goodsId);
        int stockCount = goods.getStockCount();
        if (stockCount <= 0) {
            return;
        }
        //判断是否重复下单
        SeckillOrder seckillOrder = orderServiceApi.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (seckillOrder != null) {
            return;
        }
        //减库存，下订单，写入秒杀订单表
        seckillServiceApi.seckill(user, goods);
    }
}
