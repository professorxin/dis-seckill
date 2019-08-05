package com.lzx.common.api.cache.vo;

/**
 * redis键的前缀，为了避免重复的key出现，在key前面加上该前缀
 */
public interface KeyPrefix {


    /**
     * key的过期时间
     * @return
     */
    public int expireSeconds();

    /**
     * 获取key的前缀
     * @return
     */
    public String getPrefix();

}
