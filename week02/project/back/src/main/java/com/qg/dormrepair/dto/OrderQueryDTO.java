package com.qg.dormrepair.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.qg.dormrepair.constants.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报修单多条件查询 DTO
 */
@Data
@Schema(description = "报修单多条件查询请求参数")
public class OrderQueryDTO {

    @Schema(description = "报修单ID", example = "1001")
    private Long id;

    // 可选：按状态查询
    @Schema(description = "报修状态 1-待处理 2-已完成 3-已取消", example = "1")
    @Pattern(regexp = RegexConstants.ORDER_STATUS, message = "状态参数错误")
    private String status;

    // 可选：按宿舍楼栋查询
    @Schema(description = "宿舍楼栋", example = "A栋")
    private String dormBuilding;

    // 可选：按宿舍房间号查询
    @Schema(description = "宿舍房间号", example = "101")
    private String dormRoom;

    // 可选：按时间查询
    // 接收前端 yyyy-MM-dd HH:mm:ss 格式的时间
    @Schema(description = "开始时间", example = "2025-01-01 00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2025-12-31 23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    // 可选：按优先级查询
    @Schema(description = "优先级 1-普通 2-紧急 3-非常紧急", example = "1")
    @Pattern(regexp = RegexConstants.PRIORITY, message = "优先级参数错误")
    private String priority;

    // 可选：按设备类型查询
    @Schema(description = "设备类型 1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表", example = "1")
    @Pattern(regexp = RegexConstants.DEVICE_TYPE, message = "设备类型参数错误")
    private String deviceType;

    // 分页参数,默认1，10
    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小值为1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    @Min(value = 1, message = "每页数量最小值为1")
    private Integer pageSize = 10;
}