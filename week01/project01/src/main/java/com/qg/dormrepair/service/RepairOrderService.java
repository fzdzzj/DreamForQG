package com.qg.dormrepair.service;

import com.qg.dormrepair.dto.CreateOrderDTO;
import com.qg.dormrepair.dto.OrderQueryDTO;
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
     * @param orderDTO 订单创建参数，包含设备类型、问题描述、优先级等核心信息
     * @throws BusinessException 学生未绑定宿舍/参数非法/订单创建失败时抛出对应业务异常
     */
     void createOrder(CreateOrderDTO orderDTO);
    /**
     * 查询当前登录用户的所有订单
     * <p>
     * - 学生角色：仅返回当前账号提交的订单列表；
     * - 管理员角色：返回全量订单列表（该方法管理员端建议使用getAllOrders替代）
     * </p>
     *
     * @return 符合权限的订单列表，无订单时返回空列表
     */
     List<RepairListVO> getOrdersByAccount();
    /**
     * 学生查询订单详情
     * <p>
     * - 学生角色：仅能查询本人提交的订单，非本人订单抛「无权限」异常；
     * </p>
     *
     * @param id 订单唯一标识（Long类型）
     * @return 订单详情对象
     */
    RepairOrderVO getOrderById(Long id);
    /**
     * 查询指定维修订单
     *
     * @return 指定订单详细
     * @throws BusinessException 获得数据为空，「不存在」业务异常
     */
    List<RepairListVO> getAllOrders();
    /**
     * 按状态筛选订单（仅管理员可用）
     * <p>
     * 筛选指定状态的订单列表，状态码含义：1-待维修 2-已完成 3-已取消
     * </p>
     *
     * @param status 订单状态（Character类型，仅支持1/2/3）
     * @return 指定状态的订单列表，无符合条件订单时返回空列表
     */
    List<RepairListVO> getOrdersByStatus(Character status);

    /**
     * 修改订单状态（仅管理员可用）
     * <p>
     * 管理员更新指定订单的处理状态，仅允许在合法状态间切换（如待维修→已完成、待维修→已取消）
     * </p>
     * @param id 订单ID
     * @param status 新状态（Character类型，1-待维修 2-已完成 3-已取消）
     * @throws BusinessException 订单不存在/状态非法/非管理员角色/状态切换不合法时抛出对应业务异常
     */
    void updateOrderStatus(Long id,Character status);
    /**
     * 取消订单（仅学生可用，且订单为「待维修」状态）
     * <p>
     * 学生取消本人提交的未处理订单，取消后订单状态改为「已取消」（状态码3）
     * </p>
     * @param id 订单ID
     * @throws BusinessException 订单不存在/非本人订单/订单已完成/非待维修状态时抛出对应业务异常
     */
    void cancelOrder(Long id);
    /**
     * 删除订单（仅管理员可用）
     * @param id 订单ID
     * @throws BusinessException 订单不存在/非管理员角色/删除失败时抛出对应业务异常
     */
    void deleteOrder(Long id);
    /**
     * 按宿舍查询订单（仅管理员可用）
     *
     * @param dormBuilding 宿舍楼栋
     * @return 订单列表
     */
    List<RepairListVO> getOrdersByDorm(String dormBuilding);
    /**
     * 多条件查询订单（仅管理员可用）
     * <p>
     * 多条件查询订单列表，支持分页查询，返回结果包含总记录数和当前页数据
     * </p>
     * @param queryDTO 查询参数
     * @return 订单列表
     */
    PageResult<RepairListVO> queryOrders(OrderQueryDTO queryDTO);
}
