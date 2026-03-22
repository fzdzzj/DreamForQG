package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairListVO {
    private Long id;
    private Character deviceType;
    private Character status;
    private Character priority;
    private String dormBuilding;     // 列表可能需要显示楼栋
    private String dormRoom;         // 列表可能需要显示房间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
