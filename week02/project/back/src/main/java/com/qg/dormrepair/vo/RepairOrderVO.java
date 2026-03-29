package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修单详情VO（返回全部字段）
 * 用于单个报修单详情查询
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "报修单详情响应信息")
public class RepairOrderVO {

    // 报修单ID
    @Schema(description = "报修单ID", example = "1001")
    private Long id;

    // 学生账号
    @Schema(description = "学生账号", example = "3125004123")
    private String studentAccount;

    // 设备类型
    @Schema(description = "设备类型 1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表", example = "1")
    private String deviceType;

    // 报修描述
    @Schema(description = "问题描述", example = "水龙头漏水严重")
    private String description;

    // 报修单状态
    @Schema(description = "报修单状态 1-待处理 2-已完成 3-已取消", example = "1")
    private String status;

    // 报修单优先级
    @Schema(description = "优先级 1-普通 2-紧急 3-非常紧急", example = "1")
    private String priority;

    // 楼栋
    @Schema(description = "宿舍楼栋", example = "A栋")
    private String dormBuilding;

    // 房间号
    @Schema(description = "宿舍房间号", example = "101")
    private String dormRoom;

    // 图片列表
    @Schema(description = "图片URL列表", example = "[\"https://xxx.com/1.jpg\"]")
    private List<String> images;

    // 创建时间
    @Schema(description = "创建时间", example = "2025-03-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    // 修改时间
    @Schema(description = "更新时间", example = "2025-03-28 16:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}