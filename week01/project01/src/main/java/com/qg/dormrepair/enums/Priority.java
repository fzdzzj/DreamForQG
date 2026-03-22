package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    WAIT_FOR_REPAIR('1', "普通"),
    FINISHED('2', "紧急"),
    CANCELED('3', "非常紧急");
    private final Character code;
    private final String priority;

    public static String getPriority(Character code) {
        for (Priority status : Priority.values()) {
            if (status.code == code) {
                return status.priority;
            }
        }
        return null;
    }

}
