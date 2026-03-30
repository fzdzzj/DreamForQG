package com.qg.dormrepair.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qg.dormrepair.constants.MessageConstant;
import com.qg.dormrepair.dto.CreateOrderDTO;
import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.dto.UpdateOrderDTO;
import com.qg.dormrepair.enums.*;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.RepairOrderDao;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.RepairOrder;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.service.RepairOrderService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.RegexUtil;
import com.qg.dormrepair.vo.PageResult;
import com.qg.dormrepair.vo.RepairListVO;
import com.qg.dormrepair.vo.RepairOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 维修订单业务层实现类
 * <p>
 * 实现{@link RepairOrderService}接口定义的所有维修订单核心业务逻辑，
 * 依赖{@link RepairOrderDao}完成数据库操作，通过{@link CurrentHolder}获取当前登录用户信息，
 * 所有数据库写操作（创建/更新/取消/删除）均添加事务控制，保证数据一致性
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    /**
     * 维修订单数据访问层对象（构造器注入，不可变）
     */
    private final RepairOrderDao repairOrderDao;

    /**
     * 用户数据访问层对象（构造器注入，不可变）
     */
    private final UserDao userDao;

    /**
     * 消息服务对象（构造器注入，不可变）
     */
    private final MessageService messageService;

    /**
     * 创建维修订单的具体实现逻辑
     * <p>
     * 核心步骤：
     * 1. 从ThreadLocal获取当前登录学生账号；
     * 2. 组装RepairOrder对象，设置默认状态为「待维修」，填充创建/更新时间；
     * 3. 图片列表转换为JSON字符串存储；
     * 4. 插入数据库，受事务控制；
     * 5. 插入失败则抛业务异常；
     * 6. 发送提交成功消息给学生和管理员
     * </p>
     *
     * @param orderDTO 订单创建参数（设备类型、问题描述、优先级、宿舍信息、图片）
     * @throws BusinessException 数据库插入失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(CreateOrderDTO orderDTO) {
        // 从ThreadLocal获取当前登录学生账号
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("创建报修单请求，学生账号:{}", account);

        // 组装RepairOrder对象
        RepairOrder repairOrder = new RepairOrder();
        repairOrder.setStudentAccount(account);
        repairOrder.setDeviceType(orderDTO.getDeviceType());
        repairOrder.setDescription(orderDTO.getDescription());
        repairOrder.setPriority(orderDTO.getPriority());
        repairOrder.setStatus(RepairOrderStatus.WAIT_FOR_REPAIR.getCode());
//        repairOrder.setCreateTime(LocalDateTime.now());
//        repairOrder.setUpdateTime(LocalDateTime.now());

        // 处理图片：前端传 List<String>，数据库存 JSON 字符串
        List<String> imageList = orderDTO.getImages();
        if (imageList != null && !imageList.isEmpty()) {
            String imagesJson = JSON.toJSONString(imageList);
            repairOrder.setImages(imagesJson);
        } else {
            repairOrder.setImages("[]");
        }

        // 设置宿舍信息
        String dormBuilding = orderDTO.getDormBuilding();
        String dormRoom = orderDTO.getDormRoom();
        repairOrder.setDormBuilding(dormBuilding);
        repairOrder.setDormRoom(dormRoom);

        // 插入数据库
        boolean result = repairOrderDao.insert(repairOrder);
        if (!result) {
            log.error(MessageConstant.ORDER_SUBMIT_FAILED+"：数据库插入失败, 学生账号:{}, 订单信息:{}", account, repairOrder);
            throw new BusinessException(500,MessageConstant.ORDER_SUBMIT_FAILED);
        }
        log.info("创建报修单成功,学生账号:{}, 订单ID:{}", account, repairOrder.getId());

        // 发送消息通知
        messageService.sendMessage(account, "报修单提交成功", "您已成功提交报修单，请耐心等待处理", MessageType.REPAIR.getCode());
        messageService.sendToRole(Role.ADMIN.getCode(), "有新的报修单", "请及时处理", MessageType.REPAIR.getCode());
        log.info("发送报修单通知消息成功,学生账号:{}", account);
    }

    /**
     * 查询当前登录用户的所有维修订单
     * <p>
     * 核心逻辑：从ThreadLocal获取当前用户账号，调用DAO层查询该账号下的所有订单，
     * 学生角色返回本人订单，管理员角色返回全量订单（由DAO层权限控制）
     * </p>
     *
     * @return 当前用户有权限查看的订单列表，无订单时返回空列表
     */
    @Override
    public List<RepairListVO> getOrdersByAccount() {
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.debug("查询当前用户报修单列表，账号：{}", account);

        List<RepairOrder> orders = repairOrderDao.selectListByAccount(account);
        List<RepairListVO> voList = new ArrayList<>();

        for (RepairOrder order : orders) {
            voList.add(convertToListVO(order));
        }

        log.debug("查询当前用户报修单完成，共查询到 {} 条记录", voList.size());
        return voList;
    }

    /**
     * 根据订单ID查询订单详情
     * <p>
     * 核心步骤：
     * 1. 学生角色校验是否拥有该订单权限；
     * 2. 校验订单是否存在；
     * 3. 转换为VO对象返回
     * </p>
     *
     * @param id 订单唯一标识
     * @return 订单详情VO对象
     * @throws BusinessException 订单不存在或无权限时抛出
     */
    @Override
    public RepairOrderVO getOrderById(Long id) {
        log.debug("查询报修单详情，订单ID：{}", id);

        // 学生权限校验：只能查看自己的订单
        String currentAccount = CurrentHolder.getCurrentUser().getAccount();
        if (RegexUtil.isStudentId(currentAccount)) {
            if (repairOrderDao.isOrderBelongToUser(id, currentAccount) == null) {
                log.warn("权限校验失败：学生"+MessageConstant.NO_PERMISSION+"该订单，账号：{}，订单ID：{}", currentAccount, id);
                throw new BusinessException(403,MessageConstant.NO_PERMISSION);
            }
        }

        // 查询订单信息
        RepairOrder order = repairOrderDao.findById(id);
        if (order == null) {
            log.warn("查询失败："+MessageConstant.ORDER_NOT_EXIST+"，订单ID：{}", id);
            throw new BusinessException(400,MessageConstant.ORDER_NOT_EXIST);
        }

        log.debug("查询报修单详情成功，订单ID：{}", id);
        return convertToVO(order);
    }

    /**
     * 查询所有维修订单（仅管理员可用）
     * <p>
     * 调用DAO层查询全量订单，无额外业务逻辑
     * </p>
     *
     * @return 系统所有维修订单列表
     */
    @Override
    public List<RepairListVO> getAllOrders() {
        log.debug("管理员查询所有报修单列表");

        List<RepairOrder> orders = repairOrderDao.selectAll();
        List<RepairListVO> voList = new ArrayList<>();

        for (RepairOrder order : orders) {
            voList.add(convertToListVO(order));
        }

        log.debug("查询所有报修单完成，共查询到 {} 条记录", voList.size());
        return voList;
    }

    /**
     * 根据订单状态筛选查询订单（仅管理员可用）
     *
     * @param status 订单状态（1-待维修 2-已完成 3-已取消）
     * @return 指定状态的订单列表
     */
    @Override
    public List<RepairListVO> getOrdersByStatus(String status) {
        String statusName = RepairOrderStatus.getStatus(status);
        log.debug("按状态查询报修单，状态码：{}，状态名称：{}", status, statusName);

        List<RepairOrder> orders = repairOrderDao.selectByStatus(status);
        List<RepairListVO> voList = new ArrayList<>();

        for (RepairOrder order : orders) {
            voList.add(convertToListVO(order));
        }

        log.debug("按状态查询报修单完成，共查询到 {} 条记录", voList.size());
        return voList;
    }

    /**
     * 修改订单状态（仅管理员可用）
     * <p>
     * 核心步骤：
     * 1. 校验订单是否存在；
     * 2. 更新状态及更新时间；
     * 3. 发送状态变更通知给学生
     * </p>
     *
     * @param id     订单ID
     * @param status 新状态
     * @throws BusinessException 订单不存在或更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, String status) {
        String statusName = RepairOrderStatus.getStatus(status);
        log.info("管理员更新报修单状态，订单ID：{}，目标状态：{}", id, statusName);

        // 校验订单是否存在
        RepairOrder order = repairOrderDao.findById(id);
        if (order == null) {
            log.warn("更新状态失败："+MessageConstant.ORDER_NOT_EXIST+"订单ID：{}", id);
            throw new BusinessException(400,MessageConstant.ORDER_NOT_EXIST);
        }

        // 更新订单信息
        order.setStatus(status);
//        order.setUpdateTime(LocalDateTime.now());
        int updateRows = repairOrderDao.update(order);

        if (updateRows <= 0) {
            log.error(MessageConstant.ORDER_UPDATE_FAILED+"，订单ID：{}", id);
            throw new BusinessException(500,MessageConstant.ORDER_UPDATE_FAILED);
        }
        log.info("更新报修单状态成功，订单ID：{}", id);

        // 发送状态更新通知
        String deviceType = repairOrderDao.getDeviceType(id);
        String deviceTypeName = DeviceType.getDeviceName(deviceType);
        messageService.sendMessage(order.getStudentAccount(),
                "报修单状态更新",
                "您的报修单" + statusName + "\n设备类型：" + deviceTypeName,
                MessageType.REPAIR.getCode(), id);
        log.info("发送报修单状态更新通知成功，学生账号：{}", order.getStudentAccount());
    }

    /**
     * 学生取消自己的维修订单
     * <p>
     * 仅允许取消【待维修】状态的订单，校验权限与订单存在性
     * </p>
     *
     * @param id 订单ID
     * @throws BusinessException 无权限、订单不存在、取消失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        String currentAccount = CurrentHolder.getCurrentUser().getAccount();
        log.info("学生申请取消报修单，账号：{}，订单ID：{}", currentAccount, id);

        // 权限校验
        if (repairOrderDao.isOrderBelongToUser(id, currentAccount) == null) {
            log.warn("取消失败：学生"+MessageConstant.NO_PERMISSION+"该订单，账号：{}，订单ID：{}", currentAccount, id);
            throw new BusinessException(403,MessageConstant.NO_PERMISSION);
        }

        // 订单存在性校验
        RepairOrder order = repairOrderDao.findById(id);
        if (order == null) {
            log.warn("取消失败："+MessageConstant.ORDER_NOT_EXIST+"，订单ID：{}", id);
            throw new BusinessException(400,MessageConstant.ORDER_NOT_EXIST);
        }

        // 更新为已取消状态
        order.setStatus(RepairOrderStatus.CANCELED.getCode());
//        order.setUpdateTime(LocalDateTime.now());
        int updateRows = repairOrderDao.update(order);

        if (updateRows <= 0) {
            log.error(MessageConstant.ORDER_CANCEL_FAILED+"，订单ID：{}", id);
            throw new BusinessException(500,MessageConstant.ORDER_CANCEL_FAILED);
        }
        log.info("取消报修单成功，订单ID：{}", id);
    }

    /**
     * 删除维修订单（仅管理员可用，物理删除）
     *
     * @param id 订单ID
     * @throws BusinessException 订单不存在或删除失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        log.info("管理员删除报修单，订单ID：{}", id);

        // 校验订单是否存在
        RepairOrder order = repairOrderDao.findById(id);
        if (order == null) {
            log.warn("删除失败："+MessageConstant.ORDER_NOT_EXIST+"，订单ID：{}", id);
            throw new BusinessException(400,MessageConstant.ORDER_NOT_EXIST);
        }

        // 执行删除
        boolean deleteResult = repairOrderDao.deleteById(id);
        if (!deleteResult) {
            log.error(MessageConstant.DELETE_FAILED+"，订单ID：{}", id);
            throw new BusinessException(500,MessageConstant.DELETE_FAILED);
        }
        log.info("删除报修单成功，订单ID：{}", id);
    }

    /**
     * 根据楼栋查询维修订单
     *
     * @param dormBuilding 楼栋编号
     * @return 该楼栋下的所有维修订单
     */
    @Override
    public List<RepairListVO> getOrdersByDorm(String dormBuilding) {
        log.debug("按楼栋查询报修单，楼栋：{}", dormBuilding);

        List<RepairOrder> orders = repairOrderDao.selectByDormBuilding(dormBuilding);
        List<RepairListVO> voList = new ArrayList<>();

        for (RepairOrder order : orders) {
            voList.add(convertToListVO(order));
        }

        log.debug("按楼栋查询报修单完成，楼栋：{}，共 {} 条记录", dormBuilding, voList.size());
        return voList;
    }

    /**
     * 多条件分页查询维修订单
     * 支持：状态、优先级、楼栋、房间、时间等组合条件
     *
     * @param queryDTO 查询条件+分页参数
     * @return 分页结果
     */
    @Override
    public PageResult<RepairListVO> queryOrders(OrderQueryDTO queryDTO) {
        log.info("多条件分页查询报修单，查询条件：{}", queryDTO);

        // 分页参数合法性校验
        int pageNum = queryDTO.getPageNum() < 1 ? 1 : queryDTO.getPageNum();
        int pageSize = queryDTO.getPageSize() < 1 ? 10 : queryDTO.getPageSize();
        int offset = (pageNum - 1) * pageSize;

        // 分页查询数据
        List<RepairListVO> list = repairOrderDao.selectByCondition(queryDTO, offset, pageSize);
        // 查询总条数
        Long total = repairOrderDao.countByCondition(queryDTO);

        log.info("多条件分页查询完成，总条数：{}，当前页条数：{}", total, list.size());
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 学生更新自己的报修单信息
     * 仅允许更新未维修的订单信息
     *
     * @param repairOrder 更新参数
     * @throws BusinessException 未登录、无权限、订单不存在、更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(UpdateOrderDTO repairOrder) {
        String account = CurrentHolder.getCurrentUser().getAccount();
        if (account == null) {
            log.warn(MessageConstant.USER_NOT_LOGIN);
            throw new BusinessException(401,MessageConstant.USER_NOT_LOGIN);
        }

        Long orderId = repairOrder.getId();
        log.info("学生更新报修单信息，账号：{}，订单ID：{}", account, orderId);

        // 校验订单是否存在
        RepairOrder order = repairOrderDao.findById(orderId);
        if (order == null) {
            log.warn("更新失败："+MessageConstant.ORDER_NOT_EXIST+"，订单ID：{}", orderId);
            throw new BusinessException(400,MessageConstant.ORDER_NOT_EXIST);
        }

        // 校验是否本人订单
        if (!order.getStudentAccount().equals(account)) {
            log.warn("更新失败："+MessageConstant.NO_PERMISSION+"他人订单，账号：{}，订单ID：{}", account, orderId);
            throw new BusinessException(403,MessageConstant.NO_PERMISSION);
        }

        // 组装更新数据
        order.setDeviceType(repairOrder.getDeviceType());
        order.setDescription(repairOrder.getDescription());
        order.setPriority(repairOrder.getPriority());

        // 处理图片
        List<String> imageList = repairOrder.getImages();
        if (imageList != null && !imageList.isEmpty()) {
            String imagesJson = JSON.toJSONString(imageList);
            order.setImages(imagesJson);
        } else {
            order.setImages("[]");
        }

//        order.setUpdateTime(LocalDateTime.now());

        // 执行更新
        int updateRows = repairOrderDao.update(order);
        if (updateRows <= 0) {
            log.error(MessageConstant.ORDER_UPDATE_FAILED+"，订单ID：{}", orderId);
            throw new BusinessException(500,MessageConstant.ORDER_UPDATE_FAILED);
        }
        log.info("更新报修单成功，订单ID：{}", orderId);

        // 发送更新通知
        messageService.sendMessage(account, "报修单更新成功", "您的报修单信息已修改", MessageType.REPAIR.getCode());
    }

    // ==================== 私有转换方法 ====================

    /**
     * 维修订单实体转换为列表VO
     *
     * @param order 订单实体
     * @return 列表VO
     */
    private RepairListVO convertToListVO(RepairOrder order) {
        if (order == null) {
            return null;
        }
        RepairListVO vo = new RepairListVO();
        vo.setId(order.getId());
        vo.setDeviceType(String.valueOf(order.getDeviceType()));
        vo.setStatus(String.valueOf(order.getStatus()));
        vo.setPriority(String.valueOf(order.getPriority()));
        vo.setDormBuilding(order.getDormBuilding());
        vo.setDormRoom(order.getDormRoom());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    /**
     * 维修订单实体转换为详情VO
     *
     * @param order 订单实体
     * @return 详情VO
     */
    private RepairOrderVO convertToVO(RepairOrder order) {
        if (order == null) {
            return null;
        }
        RepairOrderVO vo = new RepairOrderVO();
        vo.setId(order.getId());
        vo.setStudentAccount(order.getStudentAccount());
        vo.setDeviceType(order.getDeviceType());
        vo.setDescription(order.getDescription());
        vo.setStatus(order.getStatus());
        vo.setPriority(order.getPriority());
        vo.setDormBuilding(order.getDormBuilding());
        vo.setDormRoom(order.getDormRoom());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        vo.setImages(parseImages(order.getImages()));
        return vo;
    }

    /**
     * 解析数据库中图片JSON字符串为List集合
     *
     * @param imagesJson 图片JSON字符串
     * @return 图片地址列表
     */
    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析报修单图片JSON失败，JSON内容：{}", imagesJson, e);
            return new ArrayList<>();
        }
    }
}