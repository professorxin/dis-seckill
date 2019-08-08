package com.lzx.common.api.cache;

public interface RedisLockApi {

    boolean lock(String lockKey, String uniqueValue, int expireTime);

    boolean unlock(String lockKey, String uniqueValue);
}
