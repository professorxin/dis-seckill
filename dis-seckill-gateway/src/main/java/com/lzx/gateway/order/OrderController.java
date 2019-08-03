package com.lzx.gateway.order;

import com.lzx.seckill.domain.OrderInfo;
import com.lzx.seckill.domain.SeckillUser;
import com.lzx.seckill.result.CodeMsg;
import com.lzx.seckill.result.Result;
import com.lzx.seckill.service.GoodsService;
import com.lzx.seckill.service.OrderService;
import com.lzx.seckill.vo.GoodsVo;
import com.lzx.seckill.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private GoodsService goodsService;

    @RequestMapping(value = "/detail")
    @ResponseBody
    public Result<OrderDetailVo> toDetailStatic(SeckillUser seckillUser, @RequestParam("orderId") long orderId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = orderInfo.getGoodsId();
        GoodsVo goodsVo = goodsService.getGoodsVoById(goodsId);

        OrderDetailVo orderDetailVo = new OrderDetailVo();
        orderDetailVo.setGoods(goodsVo);
        orderDetailVo.setOrder(orderInfo);
        return Result.success(orderDetailVo);
    }
}
