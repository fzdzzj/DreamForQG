package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.constants.RegexConstants;
import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.dto.OrderStatusDTO;
import com.qg.dormrepair.enums.RepairOrderStatus;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.RepairOrderService;
import com.qg.dormrepair.vo.PageResult;
import com.qg.dormrepair.vo.RepairListVO;
import com.qg.dormrepair.vo.RepairOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
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
@Tag(name = "管理员功能接口", description = "管理员对订单的增删改查操作")
@CrossOrigin(origins = "*")
public class AdminController {

    private final RepairOrderService repairOrderService;

    @GetMapping("/orders")
    @Operation(summary = "查询所有订单", description = "获取全部维修订单列表")
    public Result<List<RepairListVO>> getAllOrders() {
        log.info("查询所有订单");
        List<RepairListVO> list = repairOrderService.getAllOrders();
        log.info("查询所有订单结果:{}", list);
        return Result.success(list);
    }

    @GetMapping("/order/{id}")
    @Operation(summary = "查询订单详情", description = "根据订单ID获取维修单详细信息")
    public Result<RepairOrderVO> getOrderDetail(
            @Parameter(name = "id", description = "订单ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("查询订单id为:{}的详情", id);
        RepairOrderVO orderVO = repairOrderService.getOrderById(id);
        log.info("查询订单详细结果:{}", orderVO);
        return Result.success(orderVO);
    }

    @GetMapping("/orders/by-status")
    @Operation(summary = "按状态筛选订单", description = "1-待维修 2-已完成 3-已取消")
    public Result<List<RepairListVO>> getOrdersByStatus(
            @Parameter(description = "订单状态 1/2/3", required = true, example = "1")
            @RequestParam @Pattern(regexp = RegexConstants.ORDER_STATUS, message = "订单状态格式错误") String status) {
        log.info("按状态筛选订单，状态:{}", RepairOrderStatus.getStatus(status));
        List<RepairListVO> list = repairOrderService.getOrdersByStatus(status);
        log.info("按状态查询结果:{}", list);
        return Result.success(list);
    }

    @PutMapping("/order/{id}/status")
    @OperationLog("修改订单状态")
    @Operation(summary = "修改订单状态", description = "更新维修单当前状态")
    public Result<Void> updateOrderStatus(
            @Parameter(name = "id", description = "订单ID", required = true, example = "1")
            @PathVariable Long id,
            @Validated @RequestBody OrderStatusDTO orderStatusDTO) {
        log.info("修改订单状态，id:{},状态:{}", id, RepairOrderStatus.getStatus(orderStatusDTO.getStatus()));
        repairOrderService.updateOrderStatus(id, orderStatusDTO.getStatus());
        log.info("修改订单状态成功");
        return Result.success();
    }

    @DeleteMapping("/order/{id}")
    @OperationLog("删除订单")
    @Operation(summary = "删除订单", description = "根据ID删除维修订单")
    public Result<Void> deleteOrder(
            @Parameter(name = "id", description = "订单ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("删除订单，id:{}", id);
        repairOrderService.deleteOrder(id);
        log.info("删除订单成功");
        return Result.success();
    }

    @GetMapping("/orders/by-dorm")
    @Operation(summary = "按宿舍筛选订单", description = "根据宿舍楼栋筛选维修单")
    public Result<List<RepairListVO>> getOrdersByDorm(
            @Parameter(description = "宿舍楼栋", required = true, example = "A栋")
            @RequestParam String dormBuilding) {
        log.info("按宿舍筛选订单，宿舍:{}", dormBuilding);
        List<RepairListVO> list = repairOrderService.getOrdersByDorm(dormBuilding);
        log.info("按宿舍查询结果:{}", list);
        return Result.success(list);
    }

    @PostMapping("/orders/query")
    @Operation(summary = "多条件查询订单", description = "支持楼栋、状态、关键词等组合分页查询")
    public Result<PageResult<RepairListVO>> queryOrders(
            @RequestBody OrderQueryDTO queryDTO) {
        log.info("多条件查询订单，参数:{}", queryDTO);
        PageResult<RepairListVO> pageResult = repairOrderService.queryOrders(queryDTO);
        log.info("多条件查询结果:{}", pageResult);
        return Result.success(pageResult);
    }
}