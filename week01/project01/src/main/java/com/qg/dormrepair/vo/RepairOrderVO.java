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
    private Long id;
    private String studentAccount;
    private Character deviceType;
    private String description;
    private Character status;
    private Character priority;
    private String dormBuilding;
    private String dormRoom;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
