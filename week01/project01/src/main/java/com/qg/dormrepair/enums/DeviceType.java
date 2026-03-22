package com.qg.dormrepair.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 * 设备类型枚举类
 */
@Getter
@AllArgsConstructor
public enum DeviceType {
    FAUCET('1',"水龙头"),
    TOILET('2',"马桶"),
    LIGHT('3',"电灯"),
    WINDOW('4',"窗"),
    DOOR('5',"门"),
    BED('6',"床"),
    SINK('7',"水槽"),
    ELECTRIC_METER('8',"电表"),
    WATER_METER('9',"水表");
    private final Character code;
    private final String name;
    public static String getDeviceName(Character code){
        if(code==null){
            return "未知设施类型";
        }
        for(DeviceType deviceType : DeviceType.values()){
            if(deviceType.code.equals(code)){
                return deviceType.name;
            }
        }
        return "未知设施类型";
    }
}
