package com.lzx.common.api.cache.vo;

public class UserKeyPrefix extends BaseKeyPrefix{

    public UserKeyPrefix(String prefix){
        super(prefix);
    }

    public static UserKeyPrefix getById = new UserKeyPrefix("id");

}
