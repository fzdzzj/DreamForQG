package com.qg.dormrepair.service;

import com.qg.dormrepair.dto.CreateOrderDTO;
import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.dto.UpdateOrderDTO;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.vo.PageResult;
import com.qg.dormrepair.vo.RepairListVO;
import com.qg.dormrepair.vo.RepairOrderVO;

import java.util.List;

/**
 * 维修订单业务层核心接口
 * <p>
 * 定义维修订单全生命周期的核心业务逻辑规范，包括订单创建、查询、状态修改、取消、删除等功能，
 * 区分学生/管理员不同角色的操作权限，所有异常统一抛出{@link BusinessException}
 * </p>
 */
public interface RepairOrderService {

    /**
     * 创建维修订单
     * <p>
     * 学生提交报修订单，自动关联当前登录学生的账号和已绑定的宿舍信息，
     * 订单默认状态为「待维修」（状态码1）
     * </p>
     *
     * @param orderDTO 订单创建参数，包含设备类型、问题描述、优先级等核心信息
     * @throws BusinessException 学生未绑定宿舍/参数非法/订单创建失败时抛出对应业务异常
     */
    void createOrder(CreateOrderDTO orderDTO);

    /**
     * 查询当前登录用户的所有订单
     * <p>
     * - 学生角色：仅返回当前账号提交的订单列表；
     * - 管理员角色：返回全量订单列表
     * </p>
     *
     * @return 符合权限的订单列表，无订单时返回空列表
     */
    List<RepairListVO> getOrdersByAccount();

    /**
     * 根据订单ID查询订单详情
     * <p>
     * 学生只能查看自己的订单，管理员可查看所有订单
     * </p>
     *
     * @param id 订单唯一标识
     * @return 订单详情VO对象
     * @throws BusinessException 订单不存在或无权限时抛出
     */
    RepairOrderVO getOrderById(Long id);

    /**
     * 查询所有维修订单（仅管理员可用）
     *
     * @return 全量订单列表
     * @throws BusinessException 无权限时抛出
     */
    List<RepairListVO> getAllOrders();

    /**
     * 按状态筛选订单（仅管理员可用）
     * <p>
     * 状态码：1-待维修 2-已完成 3-已取消
     * </p>
     *
     * @param status 订单状态
     * @return 指定状态的订单列表
     */
    List<RepairListVO> getOrdersByStatus(String status);

    /**
     * 修改订单状态（仅管理员可用）
     *
     * @param id     订单ID
     * @param status 新状态
     * @throws BusinessException 订单不存在/更新失败时抛出
     */
    void updateOrderStatus(Long id, String status);

    /**
     * 学生取消自己的维修订单
     * 仅允许取消【待维修】状态的订单
     *
     * @param id 订单ID
     * @throws BusinessException 无权限、订单不存在、取消失败时抛出
     */
    void cancelOrder(Long id);

    /**
     * 删除维修订单（仅管理员可用）
     *
     * @param id 订单ID
     * @throws BusinessException 订单不存在/删除失败时抛出
     */
    void deleteOrder(Long id);

    /**
     * 根据楼栋查询维修订单
     *
     * @param dormBuilding 楼栋编号
     * @return 该楼栋下的订单列表
     */
    List<RepairListVO> getOrdersByDorm(String dormBuilding);

    /**
     * 多条件分页查询维修订单
     * 支持状态、优先级、楼栋、房间、时间等组合条件
     *
     * @param queryDTO 查询条件
     * @return 分页订单列表
     */
    PageResult<RepairListVO> queryOrders(OrderQueryDTO queryDTO);

    /**
     * 学生更新自己的报修单信息
     *
     * @param repairOrder 更新参数
     * @throws BusinessException 未登录、无权限、订单不存在时抛出
     */
    void updateOrder(UpdateOrderDTO repairOrder);
}