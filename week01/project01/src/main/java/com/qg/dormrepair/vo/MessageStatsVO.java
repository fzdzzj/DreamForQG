package com.qg.dormrepair.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 消息统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageStatsVO {
    private Long totalCount;//总消息数
    private Long unreadCount;//未读消息数
    private Long todayCount;//今日消息数
}
