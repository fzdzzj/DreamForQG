package com.qg.dormrepair.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    //序列化时忽略，将Java对象转为JSON或者XML时，不包含密码字段
    @JsonIgnore
    private String pwd;
    //角色
    private Character role;
    //账号
    private String account;
    //宿舍楼
    private String dormBuilding;
    //宿舍号
    private String dormRoom;
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", pwd='" + pwd + '\'' +
                ", role=" + role +
                ", account='" + account + '\'' +
                ", dormBuilding='" + dormBuilding + '\'' +
                ", dormRoom='" + dormRoom + '\'' +
                '}';
    }
}
