package com.lzx.gateway.order;

import com.lzx.common.api.goods.GoodsServiceApi;
import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.api.order.OrderServiceApi;
import com.lzx.common.api.order.vo.OrderDetailVo;
import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.result.CodeMsg;
import com.lzx.common.result.Result;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Reference
    private OrderServiceApi orderServiceApi;

    @Reference
    private GoodsServiceApi goodsServiceApi;

    @RequestMapping(value = "/detail")
    @ResponseBody
    public Result<OrderDetailVo> toDetailStatic(SeckillUser seckillUser, @RequestParam("orderId") long orderId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo orderInfo = orderServiceApi.getOrderById(orderId);
        if (orderInfo == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsServiceApi.getGoodsVoById(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoods(goodsVo);
        orderDetailVo.setOrder(orderInfo);
        return Result.success(orderDetailVo);
    }
}
