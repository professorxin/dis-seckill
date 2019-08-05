package com.lzx.mq.service;

import com.lzx.common.api.mq.MqProviderApi;
import com.lzx.common.util.JsonUtil;
import com.lzx.mq.config.MQConfig;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MqProviderImpl implements MqProviderApi {

    private static Logger log = LoggerFactory.getLogger(MqProviderImpl.class);

    @Autowired
    private AmqpTemplate amqpTemplate;


    public void sendSeckillMsg(Object message) {
        String mes = JsonUtil.beanToString(message);
        log.info("send seckill message:" + mes);
        amqpTemplate.convertAndSend(MQConfig.SECKILL_QUEUE, mes);
    }


}
