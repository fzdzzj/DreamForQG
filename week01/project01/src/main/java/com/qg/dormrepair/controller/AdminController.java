package com.qg.dormrepair.controller;

import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.dto.OrderStatusDTO;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.RepairOrderService;
import com.qg.dormrepair.vo.PageResult;
import com.qg.dormrepair.vo.RepairListVO;
import com.qg.dormrepair.vo.RepairOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员功能控制器
 * <p>
 * 提供管理员端维修订单的查询、详情查看、状态筛选、状态修改、删除等核心接口，
 * 所有接口均以 /api/admin 为前缀，仅对管理员角色开放
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    /**
     * 维修订单业务层服务对象（构造器注入，不可变）
     */
    private final RepairOrderService repairOrderService;

    /**
     * 查询所有维修订单
     * <p>
     * 管理员查看系统中所有报修订单，无分页，返回全量订单列表
     * </p>
     *
     * @return 统一响应结果，数据体为维修订单列表
     */
    @GetMapping("/orders")
    public Result<List<RepairListVO>> getAllOrders() {
        return Result.success(repairOrderService.getAllOrders());
    }

    /**
     * 查询订单详情
     * <p>
     * 根据订单ID查询单个维修订单的完整信息（包括设备类型、问题描述、优先级、状态等）
     * </p>
     *
     * @param id 订单ID（路径参数）
     * @return 统一响应结果，数据体为单个订单详情
     */
    @GetMapping("/order/{id}")
    public Result<RepairOrderVO> getOrderDetail(@PathVariable Long id) {
        return Result.success(repairOrderService.getOrderById(id));
    }

    /**
     * 按状态筛选维修订单
     * <p>
     * 说明：@RequestParam 接收的字符串会自动转换为Character（仅当参数为单个字符时生效），
     * 例如前端传 status=1，会自动转为 Character '1'；若传多个字符会抛出类型转换异常
     * </p>
     *
     * @param status 订单状态（请求参数，1-待维修 2-已完成 3-已取消）
     * @return 统一响应结果，数据体为指定状态的订单列表
     */
    @GetMapping("/orders/by-status")
    public Result<List<RepairListVO>> getOrdersByStatus(@RequestParam Character status) {
        return Result.success(repairOrderService.getOrdersByStatus(status));
    }

    /**
     * 修改订单状态
     * <p>
     * 管理员更新指定订单的处理状态，入参需通过JSR380校验确保状态格式合法
     * </p>
     *
     * @param id             订单ID（路径参数）
     * @param orderStatusDTO 订单状态DTO（请求体），包含合法的状态值（1-待维修 2-已完成 3-已取消）
     * @return 统一响应结果，无返回数据
     */
    @PutMapping("/order/{id}/status")
    public Result<Void> updateOrderStatus(@PathVariable Long id,
                                           @Validated @RequestBody OrderStatusDTO orderStatusDTO) {
        repairOrderService.updateOrderStatus(id, orderStatusDTO.getStatus().charAt(0));
        return Result.success();
    }

    /**
     * 删除维修订单
     * <p>
     * 物理删除指定ID的维修订单
     * </p>
     *
     * @param id 订单ID（路径参数）
     * @return 统一响应结果，无返回数据
     */
    @DeleteMapping("/order/{id}")
    public Result<Void> deleteOrder(@PathVariable Long id) {
        repairOrderService.deleteOrder(id);
        return Result.success();
    }

    /**
     * 按宿舍筛选维修订单
     * <p>
     * 筛选指定宿舍的订单列表
     * </p>
     *
     * @param dormBuilding 宿舍楼栋（请求参数）
     * @return 统一响应结果，数据体为指定宿舍的订单列表
     */
    @GetMapping("/orders/by-dorm")
    public Result<List<RepairListVO>> getOrdersByDorm(@RequestParam String dormBuilding) {
        return Result.success(repairOrderService.getOrdersByDorm(dormBuilding));
    }

    /**
     * 多条件查询订单
     * <p>
     *     多条件查询订单列表，支持分页查询，返回结果包含总记录数和当前页数据
     * </p>
     * @param queryDTO 查询参数
     * @return 统一响应结果，数据体为查询结果
     */
    @PostMapping("/orders/query")
    public Result<PageResult<RepairListVO>> queryOrders(
            @RequestBody OrderQueryDTO queryDTO) {
        return Result.success(repairOrderService.queryOrders(queryDTO));
    }
}
