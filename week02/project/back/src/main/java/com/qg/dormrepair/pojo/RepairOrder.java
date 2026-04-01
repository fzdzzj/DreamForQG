package com.qg.dormrepair.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修单实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairOrder {
    //报修单 ID
    private Long id;
    //宿舍楼栋
    private String dormBuilding;
    private String dormRoom;
    //学生账号
    private String studentAccount;
    //设备类型
    private String deviceType;
    //描述
    private String description;
    //创建时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;
    //图片
    private String images;//JSON数组存储图片
    //优先级
    private String priority;
    //状态
    private String status;
}
