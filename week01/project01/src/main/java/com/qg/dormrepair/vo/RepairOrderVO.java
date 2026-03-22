package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报修单详情VO（返回全部字段）
 * 用于单个报修单详情查询
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairOrderVO {
    // 报修单ID
    private Long id;
    // 学生账号
    private String studentAccount;
    // 设备类型
    private Character deviceType;
    // 报修描述
    private String description;
    // 报修单状态
    private Character status;
    // 报修单优先级
    private Character priority;
    // 楼栋
    private String dormBuilding;
    private String dormRoom;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
