package com.qg.dormrepair.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 消息实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private Long id;                    // 消息 ID
    private String userAccount;         // 接收者账号
    private String title;               // 消息标题
    private String content;             // 消息内容
    private String type;             // 消息类型 (1-系统 2-报修 3-公告)
    private String isRead="2";           // 是否已读 (0-未读 1-已读)
    private Long relatedId;             // 关联 ID(如报修单 ID)
    private LocalDateTime createTime;   // 创建时间
}
