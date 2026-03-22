package com.qg.dormrepair.controller;

import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.vo.MessageVO;
import com.qg.dormrepair.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(){
        return Result.success(messageService.getUnreadCount());
    }
    @GetMapping("/list")
    public Result<PageResult<MessageVO>> getMessages(@RequestParam(required = false)Character isRead,@RequestParam(defaultValue ="1")Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize){
        return Result.success(messageService.getMessages(isRead,pageNum,pageSize));
    }

    @PutMapping("/read/id")
    public Result<Void> markAsRead(@PathVariable Long id){
        messageService.markAsRead(id);
        return Result.success();
    }
    @PutMapping("read-all")
    public Result<Void> markAllAsRead(){
        messageService.markAllAsRead();
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id){
        messageService.deleteMessage(id);
        return Result.success();
    }
}
