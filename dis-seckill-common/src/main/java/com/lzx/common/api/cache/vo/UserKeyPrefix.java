package com.lzx.common.api.cache.vo;

import java.io.Serializable;

public class UserKeyPrefix extends BaseKeyPrefix implements Serializable {

    public UserKeyPrefix(String prefix){
        super(prefix);
    }

    public static UserKeyPrefix getById = new UserKeyPrefix("id");

}
