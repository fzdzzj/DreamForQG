package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 报修订单优先级枚举类
 */
@Getter
@AllArgsConstructor
public enum Priority {
    WAIT_FOR_REPAIR('1', "普通"),
    FINISHED('2', "紧急"),
    CANCELED('3', "非常紧急");
    private final Character code;
    private final String priority;

    public static String getPriority(Character code) {
       if(code == null){
           return "未知优先级类型";
       }
        for (Priority status : Priority.values()) {
            if (status.code == code) {
                return status.priority;
            }
        }
        return "未知优先级类型";
    }

}
