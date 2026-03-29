package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.MessageDao;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.Message;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageDao messageDao;
    private final UserDao userDao;

    /**
     * 发送消息
     *
     * @param userAccount 接收消息的用户
     * @param title       消息标题
     * @param content     消息内容
     * @param type        消息类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userAccount, String title, String content, String type) {
        this.sendMessage(userAccount, title, content, type, null);
    }

    /**
     * 发送消息
     *
     * @param userAccount 接收消息的用户
     * @param title       消息标题
     * @param content     消息内容
     * @param type        消息类型
     * @param relatedId   关联ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userAccount, String title, String content, String type, Long relatedId) {
        log.info("开始发送消息，接收账号：{}，标题：{}，类型：{}，关联ID：{}",
                userAccount, title, type, relatedId);

        // 基础参数校验
        if (userAccount == null || userAccount.isBlank()) {
            log.error("发送消息失败：接收用户账号不能为空");
            throw new BusinessException("接收用户账号不能为空");
        }
        if (title == null || title.isBlank()) {
            log.error("发送消息失败：消息标题不能为空");
            throw new BusinessException("消息标题不能为空");
        }

        // 构建消息实体
        Message message = new Message();
        message.setUserAccount(userAccount);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setIsRead("2");
        message.setCreateTime(LocalDateTime.now());

        // 保存消息
        int rows = messageDao.insert(message);
        if (rows <= 0) {
            log.error("发送消息失败，数据库插入失败，用户：{}", userAccount);
            throw new BusinessException("消息发送失败，请稍后重试");
        }

        log.info("发送消息成功，消息ID：{}，接收用户：{}", message.getId(), userAccount);
    }

    /**
     * 群发信息给角色
     *
     * @param role    角色
     * @param title   标题
     * @param content 内容
     * @param type    类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToRole(String role, String title, String content, String type) {
        log.info("开始群发消息，目标角色：{}，标题：{}，类型：{}", role, title, type);

        if (role == null || role.isBlank()) {
            log.error("群发消息失败：角色不能为空");
            throw new BusinessException("目标角色不能为空");
        }

        // 获取角色下所有用户账号
        List<String> userAccountList = userDao.findByRole(role);
        if (CollectionUtils.isEmpty(userAccountList)) {
            log.warn("群发消息结束：角色【{}】下无任何用户", role);
            return;
        }

        // 批量发送
        for (String account : userAccountList) {
            try {
                sendMessage(account, title, content, type);
            } catch (Exception e) {
                log.error("向用户【{}】群发消息失败", account, e);
            }
        }

        log.info("群发消息完成，角色：{}，总接收人数：{}", role, userAccountList.size());
    }

    /**
     * 获取未读消息数量
     *
     * @return 未读消息数量
     */
    @Override
    public Long getUnreadCount() {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("查询用户【{}】未读消息数量", currentUser.getAccount());

        Long unreadCount = messageDao.countUnread(currentUser.getAccount());
        log.info("用户【{}】未读消息数量：{}", currentUser.getAccount(), unreadCount);
        return unreadCount;
    }

    /**
     * 获取消息统计信息
     *
     * @return 消息统计信息
     */
    @Override
    public MessageStatsVO getStats() {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("查询用户【{}】消息统计数据", currentUser.getAccount());

        Long totalCount = messageDao.countTotal(currentUser.getAccount());
        Long unreadCount = messageDao.countUnread(currentUser.getAccount());
        Long todayCount = messageDao.countToday(currentUser.getAccount());

        MessageStatsVO statsVO = new MessageStatsVO(totalCount, unreadCount, todayCount);
        log.info("用户【{}】消息统计：总条数={}，未读={}，今日={}",
                currentUser.getAccount(), totalCount, unreadCount, todayCount);
        return statsVO;
    }

    /**
     * 获取消息列表
     *
     * @param isRead    是否已读
     * @param pageNum   页码
     * @param pageSize  页大小
     * @param type      消息类型
     * @return 消息列表分页结果
     */
    @Override
    public PageResult<MessageVO> getMessages(String isRead, Integer pageNum, Integer pageSize, String type) {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        // 分页参数修正
        pageNum = handlePageNum(pageNum);
        pageSize = handlePageSize(pageSize);

        log.info("开始查询消息列表，用户：{}，已读状态：{}，类型：{}，页码：{}，页大小：{}",
                currentUser.getAccount(), isRead, type, pageNum, pageSize);

        int offset = (pageNum - 1) * pageSize;
        // 查询数据
        List<Message> messageList = messageDao.selectByAccountWithPage(
                currentUser.getAccount(), isRead, type, offset, pageSize);
        Long total = messageDao.countTotal(currentUser.getAccount());

        log.info("查询消息列表完成，用户：{}，总条数：{}，当前页数量：{}",
                currentUser.getAccount(), total, messageList.size());

        // 转换VO
        List<MessageVO> voList = new ArrayList<>(messageList.size());
        for (Message message : messageList) {
            voList.add(convertToVO(message));
        }

        return new PageResult<>(voList, total, pageNum, pageSize);
    }

    /**
     * 标记单条消息已读
     *
     * @param messageId 消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("开始标记消息已读，用户：{}，消息ID：{}", currentUser.getAccount(), messageId);

        // 校验消息归属
        checkMessageOwnership(messageId, currentUser.getAccount());

        // 更新状态
        int rows = messageDao.updateReadStatus(messageId, currentUser.getAccount());
        if (rows <= 0) {
            log.error("标记消息已读失败，消息ID：{}", messageId);
            throw new BusinessException("标记消息已读失败，请稍后重试");
        }

        log.info("标记消息已读成功，消息ID：{}", messageId);
    }

    /**
     * 标记所有消息已读
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("开始标记用户【{}】所有消息为已读", currentUser.getAccount());

        int rows = messageDao.updateAllReadStatus(currentUser.getAccount());
        log.info("标记所有消息已读完成，用户：{}，更新条数：{}", currentUser.getAccount(), rows);
    }

    /**
     * 删除单条消息
     *
     * @param messageId 消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("开始删除消息，用户：{}，消息ID：{}", currentUser.getAccount(), messageId);

        // 校验权限
        checkMessageOwnership(messageId, currentUser.getAccount());

        int rows = messageDao.deleteById(messageId, currentUser.getAccount());
        if (rows <= 0) {
            log.error("删除消息失败，消息ID：{}", messageId);
            throw new BusinessException("删除消息失败，请稍后重试");
        }

        log.info("删除消息成功，消息ID：{}", messageId);
    }

    /**
     * 批量删除消息
     *
     * @param messageIds 消息ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> messageIds) {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("开始批量删除消息，用户：{}，消息ID数量：{}",
                currentUser.getAccount(), messageIds.size());

        if (CollectionUtils.isEmpty(messageIds)) {
            log.warn("批量删除失败：消息ID列表为空");
            throw new BusinessException("请选择需要删除的消息");
        }

        // 批量校验权限
        checkMessageOwnershipBatch(messageIds, currentUser.getAccount());

        int rows = messageDao.deleteBatch(messageIds, currentUser.getAccount());
        log.info("批量删除消息成功，用户：{}，删除条数：{}", currentUser.getAccount(), rows);
    }

    /**
     * 批量标记已读
     *
     * @param messageIds 消息ID集合
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBatchAsRead(List<Long> messageIds) {
        CurrentHolder.UserContext currentUser = getCurrentUser();
        log.info("开始批量标记已读，用户：{}，消息ID数量：{}",
                currentUser.getAccount(), messageIds.size());

        if (CollectionUtils.isEmpty(messageIds)) {
            log.warn("批量标记已读失败：消息ID列表为空");
            throw new BusinessException("请选择需要标记的消息");
        }

        // 权限校验
        checkMessageOwnershipBatch(messageIds, currentUser.getAccount());

        int rows = messageDao.markBatchAsRead(messageIds, currentUser.getAccount());
        log.info("批量标记已读成功，用户：{}，更新条数：{}", currentUser.getAccount(), rows);
    }

    // ==================== 工具方法 ====================

    /**
     * 获取当前登录用户（统一校验）
     */
    private CurrentHolder.UserContext getCurrentUser() {
         CurrentHolder.UserContext currentUser = CurrentHolder.getCurrentUser();
        if (currentUser == null || currentUser.getAccount() == null) {
            log.warn("用户未登录或登录状态失效");
            throw new BusinessException("请先登录");
        }
        return currentUser;
    }

    /**
     * 校验单条消息归属
     */
    private void checkMessageOwnership(Long messageId, String userAccount) {
        if (messageId == null || messageId <= 0) {
            throw new BusinessException("消息ID不合法");
        }

        Long count = messageDao.findMessageId(messageId, userAccount);
        if (count == null || count <= 0) {
            log.warn("权限校验失败：用户【{}】无权限操作消息【{}】", userAccount, messageId);
            throw new BusinessException(403,"无权限操作该消息");
        }
    }

    /**
     * 批量校验消息归属
     */
    private void checkMessageOwnershipBatch(List<Long> messageIds, String userAccount) {
        for (Long messageId : messageIds) {
            checkMessageOwnership(messageId, userAccount);
        }
    }

    /**
     * 处理页码
     */
    private int handlePageNum(Integer pageNum) {
        return (pageNum == null || pageNum < 1) ? 1 : pageNum;
    }

    /**
     * 处理页大小
     */
    private int handlePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    /**
     * 转换为VO
     */
    private MessageVO convertToVO(Message message) {
        if (message == null) {
            return null;
        }
        MessageVO vo = new MessageVO();
        vo.setId(message.getId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setType(message.getType());
        vo.setIsRead(message.getIsRead());
        vo.setRelatedId(message.getRelatedId());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}