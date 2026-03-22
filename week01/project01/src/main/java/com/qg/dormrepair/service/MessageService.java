// service/MessageService.java
package com.qg.dormrepair.service;

import com.qg.dormrepair.pojo.Message;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.PageResult;
import java.util.List;
/**
 * 消息服务
 */
public interface MessageService {

    /**
     * 发送消息
     * @param userAccount 用户账号
     * @param title 标题
     * @param content 内容
     * @param type 类型
     */
    void sendMessage(String userAccount, String title, String content, Character type);

    /**
     * 发送消息（带关联 ID）
     * @param userAccount 用户账号
     * @param title 标题
     * @param content 内容
     * @param type 类型
     * @param relatedId 关联 ID
     */
    void sendMessage(String userAccount, String title, String content, Character type, Long relatedId);

    /**
     * 发送消息给指定角色
     * @param role 角色
     * @param title 标题
     * @param content 内容
     * @param type 类型
     */
    void sendToRole(Character role, String title, String content, Character type);

    /**
     * 获取未读消息数
     * @return
     */
    Long getUnreadCount();

    /**
     * 获取消息统计信息
     * @return
     */
    MessageStatsVO getStats();

    /**
     * 获取消息列表
     * @param isRead 是否已读
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return
     */
    PageResult<MessageVO> getMessages(Character isRead, Integer pageNum, Integer pageSize);

    /**
     * 标记消息为已读
     * @param messageId 消息 ID
     */
    void markAsRead(Long messageId);

    /**
     * 批量删除消息
     */
    void markAllAsRead();

    /**
     * 删除消息
     * @param messageId 消息 ID
     */
    void deleteMessage(Long messageId);

    /**
     * 批量删除消息
     * @param messageIds 消息 ID 列表
     */
    void deleteBatch(List<Long> messageIds);

    /**
     * 批量标记为已读
     * @param messageIds 消息 ID 列表
     */
    void markBatchAsRead(List<Long> messageIds);
}

