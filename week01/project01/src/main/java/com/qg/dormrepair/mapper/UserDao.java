package com.qg.dormrepair.mapper;

import com.qg.dormrepair.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserDao {
    //插入用户,注册
    @Insert("insert into user (account, pwd, role, dorm_building, dorm_room) values (#{account}, #{pwd}, #{role}, #{dormBuilding}, #{dormRoom})")
    int insert(User user);
    //查询用户
    @Select("SELECT * FROM user WHERE account = #{account}")
    User findByAccount(String account);
    //登录
    @Select("select * from user where account=#{account} and pwd=#{pwd}")
    User login(String account, String pwd);
    //修改用户信息,密码

    int update(User user);

    @Select("SELECT account from user where role='1'")
    List<String> findByRole(Character role);
}
