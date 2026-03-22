package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息类型枚举类
 */
@Getter
@AllArgsConstructor
public enum MessageType {
    SYSTEM('1', "系统消息"),
    REPAIR('2', "报修消息"),
    NOTICE('3', "公告");
    private final Character code;
    private final String messageType;

    public static String getMessageType(Character code){
        if(code == null){
            return "未知消息类型";
        }
        for(MessageType messageType: MessageType.values()){
            if(messageType.code.equals(code)){
                return messageType.messageType;
            }
        }
        return "未知消息类型";
    }

}
