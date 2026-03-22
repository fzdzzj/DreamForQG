package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RepairOrderStatus {
    WAIT_FOR_REPAIR('1', "待维修"),
    FINISHED('2', "已完成"),
    CANCELED('3', "已取消");
    private final Character code;
    private final String message;

    public static String getStatus(Character code) {
        for (RepairOrderStatus status : RepairOrderStatus.values()) {
            if (status.code.equals(code)) {
                return status.message;
            }
        }
        return null;
    }
}
