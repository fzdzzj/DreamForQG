package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    STUDENT('1',"学生"),
    ADMIN('2',"管理员");
    private final Character code;
    private final String name;

    public static String getRole(Character code){
        for(Role role : Role.values()){
            if(role.code.equals(code)){
                return role.name;
            }
        }
        return null;
    }
}
