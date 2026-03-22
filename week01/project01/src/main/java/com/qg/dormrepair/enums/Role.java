package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 角色枚举类
 * <p>
 * 角色枚举类，包含学生角色和管理员角色，用于标识用户角色
 * </p>
 */
@Getter
@AllArgsConstructor
public enum Role {
    STUDENT('1',"学生"),
    ADMIN('2',"管理员");
    private final Character code;
    private final String name;

    public static String getRole(Character code){
        if (code == null){
            return "未知角色类型";
        }
        for(Role role : Role.values()){
            if(role.code.equals(code)){
                return role.name;
            }
        }
        return "未知角色类型";
    }
}
