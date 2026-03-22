package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * 报修单列表VO
 * 用于报修单列表查询
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairListVO {
    // 报修单 ID
    private Long id;
    // 设备类型
    private Character deviceType;
    // 状态
    private Character status;
    //优先级
    private Character priority;
    // 楼栋
    private String dormBuilding;
    private String dormRoom;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
