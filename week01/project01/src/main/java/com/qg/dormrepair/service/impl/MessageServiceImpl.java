package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.mapper.MessageDao;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.Message;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.util.CurrentHolder;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    private final MessageDao messageDao;
    private final UserDao userDao;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userAccount, String title, String content, Character type) {
        sendMessage(userAccount,title,content,type,null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendMessage(String userAccount, String title, String content, Character type, Long relatedId) {
        log.info("发送消息,用户:{},标题:{}",userAccount,title);
        Message message=new Message();
        message.setUserAccount(userAccount);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setRelatedId(relatedId);
        message.setCreateTime(LocalDateTime.now());

        int result = messageDao.insert(message);
        if(result<=0){
            log.error("发送消息失败");
            throw new RuntimeException("发送消息失败");
        }
        log.info("发送消息成功,ID",message.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendToRole(Character role, String title, String content, Character type) {
        log.info("群发信息给角色:{},标题{}",role,title);
        List<String> users=userDao.findByRole(role);
        for (String user : users) {
            sendMessage(user,title,content,type);
        }
        log.info("群发信息成功,接收人数:{}",users.size());
    }

    @Override
    public Long getUnreadCount() {
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        if (userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        return messageDao.countUnread(userAccount);
    }

    @Override
    public MessageStatsVO getStats(String userAccount) {
        Long totalCount= messageDao.countTotal(userAccount);
        Long unreadCount= messageDao.countUnread(userAccount);
        Long todayCount= messageDao.countToday(userAccount);
        return new MessageStatsVO(totalCount,unreadCount,todayCount);
    }

    @Override
    public PageResult<MessageVO> getMessages(Character isRead, Integer pageNum, Integer pageSize) {
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        if(pageNum==null||pageNum<1) pageNum=1;
        if(pageSize==null||pageSize<1) pageSize=10;

        int offset=(pageNum-1)*pageSize;
        List<Message> messages=messageDao.selectByAccountWithPage(userAccount,isRead,offset,pageSize);
        Long total=messageDao.countTotal(userAccount);
        List<MessageVO> list=new ArrayList<>();
        for (Message message : messages) {
            list.add(convertToVO(message));
        }
        return new PageResult<>(list,total,pageNum,pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long messageId) {
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        log.info("标记消息为已读,用户:{},ID:{}",userAccount,messageId);
        int result = messageDao.updateReadStatus(messageId,userAccount);
        if(result<=0){
            log.error("标记消息为已读失败");
            throw new RuntimeException("标记消息为已读失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        log.info("标记所有消息为已读,用户:{}",userAccount);
        int result = messageDao.updateAllReadStatus(userAccount);
        if(result<=0){
            log.error("标记所有消息为已读失败");
            throw new RuntimeException("标记所有消息为已读失败");
        }
        log.info("标记所有消息为已读成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessage(Long messageId) {
        String userAccount= CurrentHolder.getCurrentUser().getAccount();
        if(userAccount==null){
            log.warn("当前用户未登录");
            throw new RuntimeException("当前用户未登录");
        }
        log.info("删除消息,用户:{},ID:{}",userAccount,messageId);
        int result = messageDao.deleteById(messageId,userAccount);
        if(result<=0){
            log.error("删除消息失败");
            throw new RuntimeException("删除消息失败");
        }
    }
    private MessageVO convertToVO(Message message) {
        if (message == null) return null;

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
