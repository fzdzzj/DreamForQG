package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "消息信息响应体")
public class MessageVO {

    //消息ID
    @Schema(description = "消息ID", example = "1001")
    private Long id;

    //消息标题
    @Schema(description = "消息标题", example = "报修单处理通知")
    private String title;

    //消息内容
    @Schema(description = "消息内容", example = "你的报修单已完成维修")
    private String content;

    //消息类型
    @Schema(description = "消息类型 1-系统消息 2-报修单消息 3-公告", example = "2")
    private String type;

    //是否已读
    @Schema(description = "是否已读 1-已读 2-未读", example = "2")
    private String isRead;

    //关联ID
    @Schema(description = "关联报修单ID", example = "2001")
    private Long relatedId;

    //创建时间
    @Schema(description = "创建时间", example = "2025-03-28 15:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}