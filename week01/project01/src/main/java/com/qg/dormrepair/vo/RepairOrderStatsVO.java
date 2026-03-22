package com.qg.dormrepair.vo;
/**
 * 报修单统计 VO
 */
public class RepairOrderStatsVO {
    private String dormBuilding;      // 楼栋
    private Long totalCount;          // 总数量
    private Long waitForRepairCount;  // 待维修数量
    private Long completedCount;      // 已完成数量
    private Long cancelledCount;      // 已取消数量
}
