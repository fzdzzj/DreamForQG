package com.qg.dormrepair.service;

import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.PageResult;

import java.util.List;

/**
 * 消息服务接口
 * 定义消息发送、查询、已读标记、删除等核心业务方法
 */
public interface MessageService {

    /**
     * 发送普通消息
     *
     * @param userAccount 接收消息的用户账号
     * @param title       消息标题
     * @param content     消息内容
     * @param type        消息类型
     */
    void sendMessage(String userAccount, String title, String content, String type);

    /**
     * 发送带业务关联ID的消息（如报修单等关联消息）
     *
     * @param userAccount 接收消息的用户账号
     * @param title       消息标题
     * @param content     消息内容
     * @param type        消息类型
     * @param relatedId   关联业务ID（如订单ID、报修单ID）
     */
    void sendMessage(String userAccount, String title, String content, String type, Long relatedId);

    /**
     * 根据角色群发消息（如发送给全部管理员/全部学生）
     *
     * @param role    目标角色编码
     * @param title   消息标题
     * @param content 消息内容
     * @param type    消息类型
     */
    void sendToRole(String role, String title, String content, String type, Long relatedId);

    /**
     * 获取当前登录用户的未读消息总数
     *
     * @return 未读消息数量
     */
    Long getUnreadCount();

    /**
     * 获取当前登录用户的消息统计信息（未读/已读/总数）
     *
     * @return 消息统计VO对象
     */
    MessageStatsVO getStats();

    /**
     * 分页条件查询当前用户的消息列表
     *
     * @param isRead      是否已读（2-未读 1-已读 null-全部）
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @param userAccount 用户账号
     * @return 分页消息列表
     */
    PageResult<MessageVO> getMessages(String isRead, Integer pageNum, Integer pageSize, String userAccount);

    /**
     * 将单条消息标记为已读
     *
     * @param messageId 消息ID
     */
    void markAsRead(Long messageId);

    /**
     * 将当前用户所有消息标记为已读
     */
    void markAllAsRead();

    /**
     * 删除单条消息
     *
     * @param messageId 消息ID
     */
    void deleteMessage(Long messageId);

    /**
     * 批量删除多条消息
     *
     * @param messageIds 消息ID集合
     */
    void deleteBatch(List<Long> messageIds);

    /**
     * 批量将多条消息标记为已读
     *
     * @param messageIds 消息ID集合
     */
    void markBatchAsRead(List<Long> messageIds);
}