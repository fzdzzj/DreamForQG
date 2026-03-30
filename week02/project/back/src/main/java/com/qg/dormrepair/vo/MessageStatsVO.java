package com.qg.dormrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 消息统计VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "消息统计信息响应体")
public class MessageStatsVO implements Serializable {

    private static final long serialVersionUID = 1L;
    @Schema(description = "总消息数", example = "100")
    private Long totalCount;

    @Schema(description = "未读消息数", example = "10")
    private Long unreadCount;

    @Schema(description = "今日消息数", example = "5")
    private Long todayCount;
}