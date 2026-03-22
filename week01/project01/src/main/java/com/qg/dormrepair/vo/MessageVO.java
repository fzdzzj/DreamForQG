package com.qg.dormrepair.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class MessageVO {
    //消息ID
    private Long id;
    //消息标题
    private String title;
    //消息内容
    private String content;
    //消息类型
    private Character type;
    //是否已读
    private Character isRead;
    //关联ID
    private Long relatedId;
    //创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
