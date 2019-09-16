package com.lzx.user.dao;

import com.lzx.common.domain.SeckillUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeckillUserDao {

    /**
     * 根据id查询秒杀用户信息
     *
     * @param id
     * @return
     */
    @Select("SELECT * FROM seckill_user where id = #{id}")
    SeckillUser getById(@Param("id") Long id);

    @Update("UPDATE seckill_user SET password = #{password} WHERE id = #{id}")
    void updatePassword(SeckillUser updateUser);


    @Insert("INSERT INTO seckill_user (phone, nickname, password, salt, head, register_date, last_login_date, login_count) VALUES (#{phone}, #{nickname}, #{password}, #{salt}, #{head}, #{registerDate}, #{lastLoginDate}, #{loginCount}")
    long insertUser(SeckillUser seckillUser);
}
