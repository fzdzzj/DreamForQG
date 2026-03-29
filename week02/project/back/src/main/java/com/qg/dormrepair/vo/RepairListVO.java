package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 报修单列表VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "报修单列表响应信息")
public class RepairListVO {

    @Schema(description = "报修单ID", example = "1001")
    private Long id;

    @Schema(description = "设备类型 1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表", example = "1")
    private String deviceType;

    @Schema(description = "报修单状态 1-待处理 2-已完成 3-已取消", example = "1")
    private String status;

    @Schema(description = "优先级 1-普通 2-紧急 3-非常紧急", example = "1")
    private String priority;

    @Schema(description = "宿舍楼栋", example = "A栋")
    private String dormBuilding;

    @Schema(description = "宿舍房间号", example = "101")
    private String dormRoom;

    @Schema(description = "创建时间", example = "2025-03-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}