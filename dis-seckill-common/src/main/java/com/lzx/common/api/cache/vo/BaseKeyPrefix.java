package com.lzx.common.api.cache.vo;

public abstract class BaseKeyPrefix implements KeyPrefix {

    int expireSeconds;
    String prefix;


    /**
     * 只有前缀默认过期时间为0，表示永不过期
     *
     * @param prefix
     */
    public BaseKeyPrefix(String prefix) {
        this(0, prefix);
    }

    public BaseKeyPrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        String simpleName = getClass().getSimpleName();
        return simpleName + ":" + prefix;
    }


}
