package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.MessageDao;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.Message;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.RegexUtil;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    /**
     * 消息Dao
     */
    private final MessageDao messageDao;
    private final UserDao userDao;

    /**
     * 发送消息
     * @param userAccount 接收消息的用户
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userAccount, String title, String content, Character type) {
        //调用重载方法
        sendMessage(userAccount,title,content,type,null);
    }
    /**
     * 发送消息
     * @param userAccount 接收消息的用户
     * @param title 消息标题
     * @param content 消息内容
     * @param type 消息类型
     * @param relatedId 关联ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userAccount, String title, String content, Character type, Long relatedId) {
        log.info("发送消息,用户:{},标题:{}",userAccount,title);
        //创建消息
        Message message=new Message();
        message.setUserAccount(userAccount);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setCreateTime(LocalDateTime.now());
        //保存消息
        int result = messageDao.insert(message);
        //判断保存结果
        if(result<=0){
            log.error("发送消息失败");
            throw new RuntimeException("发送消息失败");
        }
        log.info("发送消息成功,ID",message.getId());
    }
    /**
     * 群发信息给角色
     * @param role 角色
     * @param title 标题
     * @param content 内容
     * @param type 类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToRole(Character role, String title, String content, Character type) {
        log.info("群发信息给角色:{},标题{}",role,title);
        //获取所有角色
        List<String> users=userDao.findByRole(role);
        //发送消息
        for (String user : users) {
            sendMessage(user,title,content,type);
        }
        log.info("群发信息成功,接收人数:{}",users.size());
    }
    /**
     * 获取未读消息数量
     * @return 未读消息数量
     */
    @Override
    public Long getUnreadCount() {
        //获取当前用户
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        //判断当前用户是否存在
        if (userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        //获取未读消息数量
        return messageDao.countUnread(userAccount);
    }
    /**
     * 获取消息统计信息
     * @return 消息统计信息
     */
    @Override
    public MessageStatsVO getStats() {
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        Long totalCount= messageDao.countTotal(userAccount);
        Long unreadCount= messageDao.countUnread(userAccount);
        Long todayCount= messageDao.countToday(userAccount);
        return new MessageStatsVO(totalCount,unreadCount,todayCount);
    }
    /**
     * 获取消息列表
     * @param isRead 是否已读
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 消息列表
     */
    @Override
    public PageResult<MessageVO> getMessages(Character isRead, Integer pageNum, Integer pageSize) {
        //获取当前用户
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        //判断当前用户是否存在
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        //判断页码和页大小
        if(pageNum==null||pageNum<1) pageNum=1;
        if(pageSize==null||pageSize<1) pageSize=10;
        int offset=(pageNum-1)*pageSize;
        log.info("获取消息列表,用户:{},已读:{},页码:{},页大小:{}",userAccount,isRead,pageNum,pageSize);
        //获取消息列表
        List<Message> messages=messageDao.selectByAccountWithPage(userAccount,isRead,offset,pageSize);
        //获取总数
        Long total=messageDao.countTotal(userAccount);
        log.info("获取消息列表成功,总数:{}",total);
        //转换为VO
        List<MessageVO> list=new ArrayList<>();
        for (Message message : messages) {
            list.add(convertToVO(message));
        }
        return new PageResult<>(list,total,pageNum,pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        //获取当前用户
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        //判断当前用户是否存在
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        log.info("标记消息为已读,用户:{},ID:{}",userAccount,messageId);
        //更新消息
        int result = messageDao.updateReadStatus(messageId,userAccount);
        //判断更新结果
        if(result<=0){
            log.error("标记消息为已读失败");
            throw new RuntimeException("标记消息为已读失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        //获取当前用户
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        //判断当前用户是否存在
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        log.info("标记所有消息为已读,用户:{}",userAccount);
        //更新消息
        int result = messageDao.updateAllReadStatus(userAccount);
        //判断更新结果
        if(result<=0){
            log.error("标记所有消息为已读失败");
            throw new RuntimeException("标记所有消息为已读失败");
        }
        log.info("标记所有消息为已读成功");
    }
    /**
     * 删除消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        //获取当前用户
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        //判断当前用户是否存在
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        if(!RegexUtil.isAdminId(userAccount)&&!messageDao.isBelongToUser(messageId,userAccount)){
            log.warn("当前用户无权限删除此消息");
            throw new RuntimeException("当前用户无权限删除此消息");
        }
        log.info("删除消息,用户:{},ID:{}",userAccount,messageId);
        //删除消息
        int result = messageDao.deleteById(messageId,userAccount);
        //判断删除结果
        if(result<=0){
            log.error("删除消息失败");
            throw new RuntimeException("删除消息失败");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<Long> messageIds) {
        //获取当前用户
        String userAccount = CurrentHolder.getCurrentUser().getAccount();
        if (userAccount == null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        log.info("批量删除消息，用户：{}, 消息 ID 数：{}", userAccount, messageIds.size());
        //批量删除消息
        int result = messageDao.deleteBatch(messageIds, userAccount);
        //判断删除结果
        if (result <= 0) {
            throw new RuntimeException("批量删除失败");
        }

        log.info("批量删除成功，删除行数：{}", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markBatchAsRead(List<Long> messageIds) {
        //获取当前用户
        String userAccount = CurrentHolder.getCurrentUser().getAccount();
        //判断当前用户是否存在
        if (userAccount == null) {
            throw new BusinessException("当前用户未登录");
        }
        log.info("批量标记已读，用户：{}, 消息 ID 数：{}", userAccount, messageIds.size());
        //批量标记已读
        int result = messageDao.markBatchAsRead(messageIds, userAccount);
        //判断结果
        if (result <= 0) {
            throw new RuntimeException("批量标记失败");
        }

        log.info("批量标记成功，更新行数：{}", result);
    }
    /**
     * 转换为VO
     * @param message 消息
     * @return VO
     */
    private MessageVO convertToVO(Message message) {
        //判断参数
        if (message == null) return null;
        //转换
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
