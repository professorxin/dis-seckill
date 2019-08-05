package com.lzx.gateway.seckill;


import com.lzx.common.api.cache.RedisServiceApi;
import com.lzx.common.api.cache.vo.GoodsKeyPrefix;
import com.lzx.common.api.cache.vo.SeckillKeyPrefix;
import com.lzx.common.api.goods.GoodsServiceApi;
import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.api.mq.MqProviderApi;
import com.lzx.common.api.mq.vo.SeckillMessage;
import com.lzx.common.api.order.OrderServiceApi;
import com.lzx.common.api.seckill.SeckillServiceApi;
import com.lzx.common.api.seckill.vo.VerifyCodeVo;
import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillOrder;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.result.CodeMsg;
import com.lzx.common.result.Result;
import com.lzx.common.util.VerifyCodeUtil;
import com.lzx.gateway.config.access.AccessLimit;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    private Logger log = LoggerFactory.getLogger(SeckillController.class);

    @Reference
    private GoodsServiceApi goodsServiceApi;

    @Reference
    private OrderServiceApi orderServiceApi;

    @Reference
    private SeckillServiceApi seckillServiceApi;

    @Reference
    private RedisServiceApi redisServiceApi;

    @Reference
    private MqProviderApi mqProviderApi;

    //内存标记，标记库存是否为空，减少redis的访问
    private Map<Long, Boolean> localOverMap = new HashMap<>();


    /**
     * 系统初始化后加载商品库存
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsVos = goodsServiceApi.listGoodsVo();
        if (goodsVos == null) {
            return;
        }
        for (GoodsVo goodsVo : goodsVos) {
            redisServiceApi.set(GoodsKeyPrefix.seckillGoodsStock, "" + goodsVo.getId(),
                    goodsVo.getStockCount());
            localOverMap.put(goodsVo.getId(), false);
        }
    }


    /**
     * 未优化，并发1000，qps为833
     *
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping("/do_seckill")
    public String doSeckill(Model model, SeckillUser user, @RequestParam("goodsId") Long goodsId) {
        if (user == null) {
            return "login";
        }
        model.addAttribute("user", user);
        //判断是否还有秒杀库存
        GoodsVo goods = goodsServiceApi.getGoodsVoById(goodsId);
        int stockCount = goods.getStockCount();
        if (stockCount <= 0) {
            model.addAttribute("errmsg", CodeMsg.SECKILL_OVER.getMsg());
            return "seckill_fail";
        }
        //判断是否重复下单
        SeckillOrder seckillOrder = orderServiceApi.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        //log.info("判断是否重复下单" + seckillOrder.toString());
        if (seckillOrder != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_SECKILL.getMsg());
            return "seckill_fail";
        }
        //减库存，下订单，写入秒杀订单表
        OrderInfo orderInfo = seckillServiceApi.seckill(user, goods);
        //log.info(orderInfo.toString());
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        return "order_detail";

    }

    @RequestMapping("/{path}/do_seckill_static")
    @ResponseBody
    public Result doSeckillStatic(SeckillUser user,
                                  @RequestParam("goodsId") Long goodsId,
                                  @PathVariable("path") String path) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证path是否正确
        boolean check = seckillServiceApi.checkPath(user, goodsId, path);
        if (!check)
            return Result.error(CodeMsg.REQUEST_ILLEGAL);// 请求非法

        //先用map进行库存判断，减少redis访问
        Boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        Long stock = redisServiceApi.decr(GoodsKeyPrefix.seckillGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        //判断是否重复下单
        SeckillOrder seckillOrder = orderServiceApi.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (seckillOrder != null) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setUser(user);
        seckillMessage.setGoodsId(goodsId);

        mqProviderApi.sendSeckillMsg(seckillMessage);
        return Result.success(0);//0表示排队中

/*        //判断是否还有秒杀库存
        GoodsVo goods = goodsService.getGoodsVoById(goodsId);
        int stockCount = goods.getStockCount();
        if (stockCount <= 0) {
            Result.error(CodeMsg.SECKILL_OVER);
        }
        //判断是否重复下单
        SeckillOrder seckillOrder = orderService.getSeckillOrderByUserIdAndGoodsId(user.getId(), goodsId);
        if (seckillOrder != null) {
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //减库存，下订单，写入秒杀订单表
        OrderInfo orderInfo = seckillService.seckill(user, goods);
        return Result.success(orderInfo);*/
    }


    @RequestMapping("/result")
    @ResponseBody
    public Result<Long> seckillResult(SeckillUser user, @RequestParam("goodsId") Long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = seckillServiceApi.getSeckillResult(user.getId(), goodsId);
        return Result.success(result);
    }


    @AccessLimit(seconds = 5, maxAccessCount = 5, needLogin = true)
    @RequestMapping(value = "/path", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(SeckillUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode", defaultValue = "1") int verifyCode
    ) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 校验验证码
        boolean check = this.checkVerifyCode(user, goodsId, verifyCode);

        if (!check)
            return Result.error(CodeMsg.VERITF_FAIL);// 检验不通过，请求非法

        // 获取秒杀路径
        String path = seckillServiceApi.createSeckillPath(user, goodsId);
        // 向客户端回传随机生成的秒杀地址
        return Result.success(path);
    }


    @RequestMapping(value = "/verifyCode", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillVerifyCode(HttpServletResponse response, SeckillUser user,
                                               @RequestParam("goodsId") long goodsId) {
        if (user == null)
            return Result.error(CodeMsg.SESSION_ERROR);

        // 创建验证码
        try {
            VerifyCodeVo verifyCodeVo = VerifyCodeUtil.createVerifyCode();
            //log.info("验证码对象计算的值：{}" + verifyCodeVo.getExpResult());
            //验证码结果存在redis中
            redisServiceApi.set(SeckillKeyPrefix.seckillVerifyCode,
                    user.getId() + "_" + goodsId, verifyCodeVo.getExpResult());
            ServletOutputStream out = response.getOutputStream();
            // 将图片写入到resp对象中
            ImageIO.write(verifyCodeVo.getImage(), "JPEG", out);
            out.close();
            out.flush();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }


    /**
     * 检验检验码的计算结果
     *
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    private boolean checkVerifyCode(SeckillUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0) {
            return false;
        }

        // 从redis中获取验证码计算结果
        Integer oldCode = redisServiceApi.get(SeckillKeyPrefix.seckillVerifyCode, user.getId() + "_" + goodsId, Integer.class);
        //log.info("redis存储的计算结果：{},手动输入的计算结果：{}", oldCode, verifyCode);
        if (oldCode == null || oldCode - verifyCode != 0) {// !!!!!!
            return false;
        }

        // 如果校验成功，则说明校验码过期，删除校验码缓存，防止重复提交同一个验证码
        redisServiceApi.delete(SeckillKeyPrefix.seckillVerifyCode, user.getId() + "_" + goodsId);
        return true;
    }

}
