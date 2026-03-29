package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.dto.BatchMessageDTO;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.vo.MessageStatsVO;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "消息接口", description = "消息查询、已读标记、删除、统计、批量操作")
@CrossOrigin(origins = "*")
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
    @Operation(summary = "获取未读消息数量", description = "获取当前登录用户的未读消息总数")
    public Result<Long> getUnreadCount() {
        log.info("获取未读消息数量");
        Long unreadCount = messageService.getUnreadCount();
        log.info("未读消息数量:{}", unreadCount);
        return Result.success(unreadCount);
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
    @Operation(summary = "分页获取消息列表", description = "支持按已读状态、消息类型筛选")
    public Result<PageResult<MessageVO>> getMessages(
            @Parameter(description = "是否已读 1=已读 2=未读", example = "2") @RequestParam(required = false) String isRead,
            @Parameter(description = "当前页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "消息类型 1=系统消息 2=报修单消息 3=公告", example = "1")@RequestParam(required = false)String  type) {
        log.info("获取消息列表");
        PageResult<MessageVO> pageResult = messageService.getMessages(isRead, pageNum, pageSize,type);
        log.info("获取消息列表成功,总数:{}", pageResult.getTotal());
        return Result.success(pageResult);
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
    @OperationLog("标记消息已读")
    @Operation(summary = "标记单条消息已读", description = "根据消息ID标记为已读")
    public Result<Void> markAsRead(
            @Parameter(description = "消息ID", required = true, example = "1001") @PathVariable Long id) {
        log.info("标记消息为已读");
        messageService.markAsRead(id);
        log.info("标记消息为已读成功");
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
    @PutMapping("/read-all")
    @OperationLog("标记所有消息已读")
    @Operation(summary = "标记全部消息已读", description = "一键标记当前用户所有消息为已读")
    public Result<Void> markAllAsRead() {
        log.info("标记所有消息为已读");
        messageService.markAllAsRead();
        log.info("标记所有消息为已读成功");
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
    @OperationLog("删除消息")
    @Operation(summary = "删除单条消息", description = "根据消息ID删除")
    public Result<Void> deleteMessage(
            @Parameter(description = "消息ID", required = true, example = "1001") @PathVariable Long id) {
        log.info("删除消息");
        messageService.deleteMessage(id);
        log.info("删除消息成功");
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
    @Operation(summary = "获取消息统计信息", description = "总消息数、已读、未读统计")
    public Result<MessageStatsVO> getStats() {
        log.info("获取消息统计信息");
        MessageStatsVO stats = messageService.getStats();
        log.info("获取消息统计信息成功,数据:{}", stats);
        return Result.success(stats);
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
    @OperationLog("批量删除消息")
    @Operation(summary = "批量删除消息", description = "根据消息ID数组批量删除")
    public Result<Void> deleteBatch(@Validated @RequestBody BatchMessageDTO dto) {
        log.info("批量删除消息");
        messageService.deleteBatch(dto.getMessageIds());
        log.info("批量删除消息成功");
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
    @OperationLog("批量标记消息已读")
    @Operation(summary = "批量标记消息已读", description = "根据消息ID数组批量已读")
    public Result<Void> markBatchAsRead(@Validated @RequestBody BatchMessageDTO dto) {
        log.info("批量标记消息为已读");
        messageService.markBatchAsRead(dto.getMessageIds());
        log.info("批量标记消息为已读成功");
        return Result.success();
    }
}