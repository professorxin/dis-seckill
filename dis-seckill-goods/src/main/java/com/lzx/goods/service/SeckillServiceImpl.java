package com.lzx.goods.service;


import com.lzx.common.api.cache.RedisServiceApi;
import com.lzx.common.api.cache.vo.SeckillKeyPrefix;
import com.lzx.common.api.goods.GoodsServiceApi;
import com.lzx.common.api.goods.vo.GoodsVo;
import com.lzx.common.api.order.OrderServiceApi;
import com.lzx.common.api.seckill.SeckillServiceApi;
import com.lzx.common.domain.OrderInfo;
import com.lzx.common.domain.SeckillOrder;
import com.lzx.common.domain.SeckillUser;
import com.lzx.common.util.MD5Util;
import com.lzx.common.util.UUIDUtil;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SeckillServiceImpl implements SeckillServiceApi {

    private static final Logger log = LoggerFactory.getLogger(SeckillServiceImpl.class);

    //@Reference   报空指针异常
    @Autowired
    private GoodsServiceApi goodsServiceApi;

    @Reference
    private OrderServiceApi orderServiceApi;

    @Reference
    private RedisServiceApi redisServiceApi;

    // 用于生成验证码中的运算符
    private char[] ops = new char[]{'+', '-', '*'};

    @Transactional
    public OrderInfo seckill(SeckillUser user, GoodsVo goods) {
        //减库存，下订单，写入秒杀订单
        //1.减库存
        boolean ret = goodsServiceApi.reduceStock(goods);
        if (!ret) {
            setGoodsOver(goods.getId());
            return null;
        }
        //2.下订单，写入秒杀订单
        return orderServiceApi.createOrder(user, goods);
    }

    private boolean getGoodsOver(Long goodsId) {
        return redisServiceApi.exists(SeckillKeyPrefix.isGoodsOver, "" + goodsId);
    }

    private void setGoodsOver(Long goodsId) {
        redisServiceApi.set(SeckillKeyPrefix.isGoodsOver, "" + goodsId, true);
    }

    public long getSeckillResult(Long userId, Long goodsId) {
        SeckillOrder seckillOrder = orderServiceApi.getSeckillOrderByUserIdAndGoodsId(userId, goodsId);
        if (seckillOrder != null) {//秒杀成功
            return seckillOrder.getOrderId();
        } else {
            boolean over = getGoodsOver(goodsId);
            if (over) {//秒杀失败
                return -1;
            } else {//排队中，等待秒杀完成
                return 0;
            }
        }
    }


    /**
     * 验证路径是否正确
     *
     * @param user
     * @param goodsId
     * @param path
     * @return
     */
    public boolean checkPath(SeckillUser user, long goodsId, String path) {
        if (user == null || path == null)
            return false;
        // 从redis中读取出秒杀的path变量是否为本次秒杀操作执行前写入redis中的path
        String oldPath = redisServiceApi.get(SeckillKeyPrefix.seckillPath, "" + user.getId() + "_" + goodsId, String.class);
        return path.equals(oldPath);
    }

    /**
     * 创建秒杀地址，并存储在redis中
     *
     * @param user
     * @param goodsId
     * @return
     */
    public String createSeckillPath(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        //随机生成秒杀地址
        String path = MD5Util.md5(UUIDUtil.uuid() + "123456");
        // 将随机生成的秒杀地址存储在redis中（保证不同的用户和不同商品的秒杀地址是不一样的）
        redisServiceApi.set(SeckillKeyPrefix.seckillPath, "" + user.getId() + "_" + goodsId, path);
        return path;
    }

    public BufferedImage createVerifyCode(SeckillUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }

        // 验证码的宽高
        int width = 80;
        int height = 32;

        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();

        // 计算表达式值，并把把验证码值存到redis中
        int expResult = calc(verifyCode);
        redisServiceApi.set(SeckillKeyPrefix.seckillVerifyCode, user.getId() + "," + goodsId, expResult);
        //输出图片
        return image;
    }

    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(exp);// 表达式计算
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
