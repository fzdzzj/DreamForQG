// mapper/MessageDao.java
package com.qg.dormrepair.mapper;

import com.qg.dormrepair.annotation.AutoFill;
import com.qg.dormrepair.enums.DataBaseOperationType;
import com.qg.dormrepair.pojo.Message;
import com.qg.dormrepair.vo.MessageVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 消息数据访问接口
 */
public interface MessageDao {

    // 插入消息
    @Insert("INSERT INTO message (user_account, title, content, type, is_read, related_id, create_time) " +
            "VALUES (#{userAccount}, #{title}, #{content}, #{type}, #{isRead}, #{relatedId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Message message);



    // 查询用户消息列表（分页 + 类型筛选 + 阅读状态筛选）
    List<Message> selectByAccountWithPage(
            @Param("userAccount") String userAccount,
            @Param("isRead") String isRead,
            @Param("type") String type,  //
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    // 统计未读消息数
    @Select("SELECT COUNT(*) FROM message WHERE user_account = #{userAccount} AND is_read = '2'")
    Long countUnread(String userAccount);

    // 统计总消息数
    @Select("SELECT COUNT(*) FROM message WHERE user_account = #{userAccount}")
    Long countTotal(String userAccount);

    // 统计今日消息数
    @Select("SELECT COUNT(*) FROM message WHERE user_account = #{userAccount} " +
            "AND DATE(create_time) = CURDATE()")
    Long countToday(String userAccount);

    // 标记单条为已读
    @Update("UPDATE message SET is_read = '1' WHERE id = #{id} AND user_account = #{userAccount}")
    int updateReadStatus(@Param("id") Long id, @Param("userAccount") String userAccount);

    // 标记全部为已读
    @Update("UPDATE message SET is_read = '1' WHERE user_account = #{userAccount} AND is_read = '2'")
    int updateAllReadStatus(String userAccount);

    // 删除消息
    @Delete("DELETE FROM message WHERE id = #{id} AND user_account = #{userAccount}")
    int deleteById(@Param("id") Long id, @Param("userAccount") String userAccount);

    // 根据 ID 查询消息
    @Select("SELECT id, user_account, title, content, type, is_read, related_id, create_time " +
            "FROM message WHERE id = #{id}")
    Message findById(Long id);

    @Select("SELECT id FROM message WHERE id = #{messageId} AND user_account = #{userAccount}")
    Long findMessageId(Long messageId, String userAccount);
    // 批量删除消息
    int deleteBatch(@Param("messageIds") List<Long> messageIds,
                    @Param("userAccount") String userAccount);
    // 批量标记为已读
    int markBatchAsRead(@Param("messageIds") List<Long> messageIds,
                        @Param("userAccount") String userAccount);
}
