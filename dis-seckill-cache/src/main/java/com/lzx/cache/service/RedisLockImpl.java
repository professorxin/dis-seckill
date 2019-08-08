package com.lzx.cache.service;

import com.lzx.common.api.cache.RedisLockApi;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Collections;

@Service
public class RedisLockImpl implements RedisLockApi {


    @Autowired
    private JedisPool jedisPool;

    private final String LOCK_SUCCESS = "OK";
    private final String SET_IF_NOT_EXIST = "NX";
    private final String SET_WITH_EXPIRE_TIME = "PX";
    private final Long RELEASE_SUCCESS = 1L;

    @Override
    public boolean lock(String lockKey, String uniqueValue, int expireTime) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String result = jedis.set(lockKey, uniqueValue, SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, expireTime);
            if (LOCK_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        } finally {
            returnToPool(jedis);
        }
    }

    //解锁分三步：get，判断，del。使用lua脚本能保证解锁的原子性
    @Override
    public boolean unlock(String lockKey, String uniqueValue) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //使用lua脚本保证操作的原子性
            String script = "if redis call('get', KEY[1]) == ARVG[1] then " +
                    "return redis.call('del',KEY[1]) " +
                    "else return 0 end";
            Object result = jedis.eval(script, Collections.singletonList(lockKey), Collections.singletonList(uniqueValue));
            if (RELEASE_SUCCESS.equals(result)) {
                return true;
            }
            return false;
        } finally {
            returnToPool(jedis);
        }
    }


    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }
}
