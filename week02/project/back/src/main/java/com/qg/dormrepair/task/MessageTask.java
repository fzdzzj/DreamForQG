package com.qg.dormrepair.task;

import com.qg.dormrepair.constants.MessageConstant;
import com.qg.dormrepair.enums.MessageType;
import com.qg.dormrepair.enums.Priority;
import com.qg.dormrepair.enums.RepairOrderStatus;
import com.qg.dormrepair.enums.Role;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageTask {
    private final MessageService messageService;
    private final RepairOrderService repairOrderService;

    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 30 * * * ?")
    public void sendUrgeMaxMessage(){
        log.info("开始发送催单消息");
        LocalDateTime time=LocalDateTime.now().plusMinutes(-15);
        List<Long> orders=repairOrderService.getOrdersByStatusAndTimeAndPriority(RepairOrderStatus.WAIT_FOR_REPAIR.getCode(),time,Priority.CANCELED.getCode());
        for (Long order:orders){
            messageService.sendToRole(Role.ADMIN.getCode(), "催单", MessageConstant.MESSAGE_URGE_MAX, MessageType.REPAIR.getCode(), order);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void sendUrgeMessage(){
        log.info("开始发送催单消息");
        LocalDateTime time=LocalDateTime.now().plusMinutes(-60);
        List<Long> orders=repairOrderService.getOrdersByStatusAndTimeAndPriority(RepairOrderStatus.WAIT_FOR_REPAIR.getCode(),time,Priority.FINISHED.getCode());
        for (Long order:orders){
            messageService.sendToRole(Role.ADMIN.getCode(), "催单", MessageConstant.MESSAGE_URGE_OVERDUE, MessageType.REPAIR.getCode(), order);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Scheduled(cron = "0 0 7-22 * * ?")
    public void sendCommonMessage(){
        log.info("开始发送催单消息");
        LocalDateTime time=LocalDateTime.now().plusMinutes(-120);
        List<Long> orders=repairOrderService.getOrdersByStatusAndTimeAndPriority(RepairOrderStatus.WAIT_FOR_REPAIR.getCode(),time,Priority.WAIT_FOR_REPAIR.getCode());
        for (Long order:orders){
            messageService.sendToRole(Role.ADMIN.getCode(), "催单", MessageConstant.MESSAGE_URGE_COMMON, MessageType.REPAIR.getCode(), order);
        }
    }


}
