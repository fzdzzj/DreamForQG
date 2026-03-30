package com.qg.dormrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报修单统计 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "报修单统计响应信息")
public class RepairOrderStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "宿舍楼栋", example = "A栋")
    private String dormBuilding;

    @Schema(description = "总数量", example = "50")
    private Long totalCount;

    @Schema(description = "待维修数量", example = "10")
    private Long waitForRepairCount;

    @Schema(description = "已完成数量", example = "35")
    private Long completedCount;

    @Schema(description = "已取消数量", example = "5")
    private Long cancelledCount;
}