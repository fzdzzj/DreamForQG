// service/MessageService.java
package com.qg.dormrepair.service;

import com.qg.dormrepair.pojo.Message;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.PageResult;
import java.util.List;

public interface MessageService {

    // 发送消息（基础）
    void sendMessage(String userAccount, String title, String content, Character type);

    // 发送消息（带关联 ID）
    void sendMessage(String userAccount, String title, String content, Character type, Long relatedId);

    // 群发消息（给所有指定角色）
    void sendToRole(Character role, String title, String content, Character type);

    // 获取未读消息数
    Long getUnreadCount();

    // 获取消息统计
    MessageStatsVO getStats(String userAccount);

    // 获取消息列表（分页）
    PageResult<MessageVO> getMessages(Character isRead, Integer pageNum, Integer pageSize);

    // 标记为已读
    void markAsRead(Long messageId);

    // 全部标记为已读
    void markAllAsRead();

    // 删除消息
    void deleteMessage(Long messageId);
}

