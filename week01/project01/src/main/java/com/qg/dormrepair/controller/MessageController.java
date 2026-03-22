package com.qg.dormrepair.controller;

import com.qg.dormrepair.dto.BatchOperationDTO;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 消息功能控制器
 * <p>
 * 提供管理员端、学生端消息的发送、接收，
 * 所有接口均以 /api/message 为前缀
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    /**
     * 消息业务层服务对象（构造器注入，不可变）
     */
    private final MessageService messageService;

    /**
     * 获取未读消息数量
     * <p>
     * 获取当前登录用户（管理员/学生）的未读消息数量
     * </p>
     *
     * @return 统一响应结果，数据体为未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(){
        log.info("获取未读消息数量");
        return Result.success(messageService.getUnreadCount());
    }
    /**
     * 获取消息列表
     * <p>
     * 获取当前登录用户（管理员/学生）的消息列表
     * </p>
     *
     * @return 统一响应结果，数据体为消息列表
     */
    @GetMapping("/list")
    public Result<PageResult<MessageVO>> getMessages(@RequestParam(required = false)Character isRead,@RequestParam(defaultValue ="1")Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize){
        log.info("获取消息列表");
        return Result.success(messageService.getMessages(isRead,pageNum,pageSize));
    }
    /**
     * 标记消息为已读
     * <p>
     * 标记指定ID的消息为已读
     * </p>
     *
     * @param id 消息ID（路径参数）
     * @return 统一响应结果，无返回数据
     */

    @PutMapping("/read/{id}")
    public Result<Void> markAsRead(@PathVariable Long id){
        log.info("标记消息为已读");
        messageService.markAsRead(id);
        return Result.success();
    }
    /**
     * 标记所有消息为已读
     * <p>
     * 标记当前登录用户（管理员/学生）的所有消息为已读
     * </p>
     *
     * @return 统一响应结果，无返回数据
     */
    @PutMapping("read-all")
    public Result<Void> markAllAsRead(){
        log.info("标记所有消息为已读");
        messageService.markAllAsRead();
        return Result.success();
    }
    /**
     * 删除消息
     * <p>
     * 删除指定ID的消息
     * </p>
     *
     * @param id 消息ID（路径参数）
     * @return 统一响应结果，无返回数据
     */

    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id){
        log.info("删除消息");
        messageService.deleteMessage(id);
        return Result.success();
    }
    /**
     * 获取消息统计信息
     * <p>
     * 获取当前登录用户（管理员/学生）的消息统计信息
     * </p>
     *
     * @return 统一响应结果，数据体为消息统计信息
     */
    @GetMapping("/stats")
    public Result<MessageStatsVO> getStats(){
        log.info("获取消息统计信息");
        return Result.success(messageService.getStats());
    }
    /**
     * 批量删除消息
     * <p>
     * 批量删除指定ID的消息
     * </p>
     *
     * @param dto 批量操作数据传输对象
     * @return 统一响应结果，无返回数据
     */
    @DeleteMapping("/batch")
    public Result<Void> deleteBatch(@Validated @RequestBody BatchOperationDTO dto) {
        messageService.deleteBatch(dto.getMessageIds());
        return Result.success();
    }
    /**
     * 批量标记消息为已读
     * <p>
     * 批量标记指定ID的消息为已读
     * </p>
     *
     * @param dto 批量操作数据传输对象
     * @return 统一响应结果，无返回数据
     */
    @PutMapping("/batch/read")
    public Result<Void> markBatchAsRead(@Validated @RequestBody BatchOperationDTO dto) {
        messageService.markBatchAsRead(dto.getMessageIds());
        return Result.success();
    }
}
