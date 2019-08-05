package com.lzx.common.api.cache;

import com.alibaba.fastjson.JSON;
import com.lzx.common.api.cache.vo.KeyPrefix;

public interface RedisServiceApi {

    /**
     * redis的get操作，通过key获取存储在redis中的对象
     *
     * @param keyPrefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    <T> T get(KeyPrefix keyPrefix, String key, Class<T> clazz);

    /**
     * redis的set操作，将对象存储在redis中
     *
     * @param keyPrefix
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    <T> boolean set(KeyPrefix keyPrefix, String key, T t) ;


    /**
     * 判断key是否存在redis中
     * @param keyPrefix
     * @param key
     * @param <T>
     * @return
     */
    <T> boolean exists(KeyPrefix keyPrefix, String key) ;


    /**
     * 自增
     * @param keyPrefix
     * @param key
     * @param <T>
     * @return
     */
    <T> Long incr(KeyPrefix keyPrefix, String key) ;

    /**
     * 自减
     * @param keyPrefix
     * @param key
     * @param <T>
     * @return
     */
    <T> Long decr(KeyPrefix keyPrefix, String key) ;

    /**
     * 根据key删除redis的数据
     * @param keyPrefix
     * @param key
     * @param <T>
     * @return
     */
    <T> boolean delete(KeyPrefix keyPrefix, String key) ;


}
