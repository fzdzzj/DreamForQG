package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.dto.CreateOrderDTO;
import com.qg.dormrepair.dto.UpdateOrderDTO;
import com.qg.dormrepair.pojo.RepairOrder;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.RepairOrderService;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.vo.RepairListVO;
import com.qg.dormrepair.vo.RepairOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生功能控制器
 * <p>
 * 提供学生端核心功能接口，包括宿舍绑定、报修订单创建/查询/取消、密码修改等，
 * 所有接口均以 /api/student 为前缀，仅对学生角色开放，接口操作均关联当前登录学生账号
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "学生功能接口", description = "学生报修单创建、查询、修改、取消、详情查看")
@CrossOrigin(origins = "*")
public class StudentController {

    /**
     * 报修订单业务层服务对象（构造器注入，不可变）
     */
    private final RepairOrderService repairOrderService;

    /**
     * 创建维修订单
     * <p>
     * 学生提交报修订单，入参需通过校验确保设备类型、优先级等参数合法，
     * 订单会自动关联当前登录学生的账号和已绑定的宿舍信息
     * </p>
     *
     * @param orderDTO 订单创建DTO（请求体），包含设备类型、问题描述、优先级等信息
     * @return 统一响应结果，无返回数据
     */
    @PostMapping("/order")
    @OperationLog("创建报修单")
    @Operation(summary = "创建报修单", description = "学生提交宿舍维修报修申请")
    public Result<Void> createOrder(@Validated @RequestBody CreateOrderDTO orderDTO) {
        log.info("创建报修单");
        repairOrderService.createOrder(orderDTO);
        log.info("创建报修单成功");
        return Result.success();
    }

    /**
     * 查询当前学生的所有报修订单
     * <p>
     * 仅返回当前登录学生提交的订单列表，无法查看其他学生的订单，
     * 订单列表包含报修记录状态、创建时间等核心信息
     * </p>
     *
     * @return 统一响应结果，数据体为当前学生的订单列表
     */
    @GetMapping("/orders")
    @Operation(summary = "查询我的报修单", description = "获取当前登录学生的所有报修订单")
    public Result<List<RepairListVO>> getMyOrders() {
        log.info("查询当前学生的所有报修订单");
        List<RepairListVO> orders = repairOrderService.getOrdersByAccount();
        log.info("查询当前学生的所有报修订单成功:{}", orders);
        return Result.success(orders);
    }

    /**
     * 查询报修详情
     * <p>
     * 学生查询自己提交的单个订单详情，仅能查看自己的订单，无法查看其他学生的订单，
     * 若传入非本人的订单ID，会抛出业务异常
     * </p>
     *
     * @param id 报修ID（路径参数）
     * @return 统一响应结果，数据体为单个报修记录详情
     */
    @GetMapping("/order/{id}")
    @Operation(summary = "查询报修单详情", description = "根据ID查看报修单详细信息")
    public Result<RepairOrderVO> getOrderDetail(
            @Parameter(description = "报修单ID", required = true, example = "1001") @PathVariable Long id) {
        log.info("查询报修单详情,ID:{}", id);
        RepairOrderVO repairOrderVO = repairOrderService.getOrderById(id);
        log.info("查询报修单详情成功:{}", repairOrderVO);
        return Result.success(repairOrderVO);
    }

    /**
     * 取消报修订单
     * <p>
     * 学生取消自己提交的报修订单，仅能取消「待维修」状态的订单，
     * </p>
     *
     * @param id 订单ID（路径参数）
     * @return 统一响应结果，无返回数据
     */
    @PutMapping("/order/{id}/cancel")
    @OperationLog("取消报修单")
    @Operation(summary = "取消报修单", description = "仅可取消待维修状态的订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "报修单ID", required = true, example = "1001") @PathVariable Long id) {
        log.info("取消报修单,ID:{}", id);
        repairOrderService.cancelOrder(id);
        log.info("取消报修单成功");
        return Result.success();
    }

    @PutMapping("/order")
    @OperationLog("修改报修单")
    @Operation(summary = "修改报修单", description = "修改未提交/待处理的报修单信息")
    public Result<Void> updateOrder(@Validated @RequestBody UpdateOrderDTO repairOrder) {
        log.info("修改报修单,ID:{}", repairOrder.getId());
        repairOrderService.updateOrder(repairOrder);
        log.info("修改报修单成功");
        return Result.success();
    }

}