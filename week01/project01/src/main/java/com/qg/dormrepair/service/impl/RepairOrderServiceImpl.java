package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.dto.CreateOrderDTO;
import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.enums.DeviceType;
import com.qg.dormrepair.enums.RepairOrderStatus;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.RepairOrderDao;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.RepairOrder;
import com.qg.dormrepair.service.MessageService;
import com.qg.dormrepair.service.RepairOrderService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.vo.PageResult;
import com.qg.dormrepair.vo.RepairListVO;
import com.qg.dormrepair.vo.RepairOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 1. 从ThreadLocal获取当前登录学生账号及已绑定的宿舍信息；
     * 2. 校验宿舍绑定状态（未绑定则抛业务异常，日志级别WARN）；
     * 3. 组装RepairOrder对象，设置默认状态为「待维修」，填充创建/更新时间；
     * 4. 插入数据库，受事务控制；
     * 5. 插入失败则抛业务异常（日志级别ERROR，标记系统级故障）
     * </p>
     * @param orderDTO 订单创建参数（设备类型、问题描述、优先级）
     * @throws BusinessException 未绑定宿舍/数据库插入失败时抛出对应业务异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(CreateOrderDTO orderDTO) {
        //1. 从ThreadLocal获取当前登录学生账号及已绑定的宿舍信息；
        String account=CurrentHolder.getCurrentUser().getAccount();
        log.info("创建报修单请求，学生账号:{}",account);
        String dormBuilding=CurrentHolder.getCurrentUser().getDormBuilding();
        String dormRoom=CurrentHolder.getCurrentUser().getDormRoom();
        //2. 校验宿舍绑定状态（未绑定则抛业务异常，日志级别WARN）；
        if(dormBuilding==null||dormRoom==null){
            log.warn("学生未绑定宿舍,学生账号:{}",account);
            throw new BusinessException("学生未绑定宿舍");
        }
        //3. 组装RepairOrder对象，设置默认状态为「待维修」，填充创建/更新时间；
        RepairOrder repairOrder=new RepairOrder();
        repairOrder.setStudentAccount(account);
        repairOrder.setDeviceType(orderDTO.getDeviceType().charAt(0));
        repairOrder.setDescription(orderDTO.getDescription());
        repairOrder.setPriority(orderDTO.getPriority().charAt(0));
        repairOrder.setStatus(RepairOrderStatus.WAIT_FOR_REPAIR.getCode());
        repairOrder.setCreateTime(LocalDateTime.now());
        repairOrder.setUpdateTime(LocalDateTime.now());
        repairOrder.setDormBuilding(dormBuilding);
        repairOrder.setDormRoom(dormRoom);
        //4. 插入数据库，受事务控制；
        boolean result=repairOrderDao.insert(repairOrder);
        //5. 插入失败则抛业务异常（日志级别ERROR，标记系统级故障）
        if(!result){
            log.error("创建报修单失败：数据库插入失败, 学生账号:{}, 订单信息:{}", account, repairOrder);
            throw new BusinessException("报修订单提交失败，请稍后重试");
        }
        log.info("创建报修单成功,学生账号:{}",account);

        List<String> admins=userDao.findByRole('2');
        for(String admin:admins){
            messageService.sendMessage(admin, "有新的报修单需要处理", "学生"+account+"提交了报修单",'2',repairOrder.getId());
        }
        log.info("发送报修单消息成功,学生账号:{}",account);
    }
    /**
     * 查询当前登录用户的所有订单的实现逻辑
     * <p>
     * 核心逻辑：从ThreadLocal获取当前用户账号，调用DAO层查询该账号下的所有订单，
     * 学生角色返回本人订单，管理员角色返回全量订单（由DAO层权限控制）
     * </p>
     *
     * @return 当前用户有权限查看的订单列表，无订单时返回空列表
     */
    @Override
    public List<RepairListVO> getOrdersByAccount() {
        String account= CurrentHolder.getCurrentUser().getAccount();
        log.debug("查询用户报修单，账号：{}",account);
        List<RepairOrder> orders= repairOrderDao.selectListByAccount(account);
        List<RepairListVO> vos=new ArrayList<>();
        for(RepairOrder order:orders){
            vos.add(convertToListVO( order));
        }
        return vos;
    }

    /**
     * 根据订单ID查询详情的实现逻辑
     * <p>
     * 核心步骤：
     * 1. 调用DAO层查询订单信息；
     * 2. 订单不存在则抛业务异常（日志级别WARN）；
     * 3. 权限控制：学生仅能查看本人订单，管理员可查看所有订单（DAO层实现）
     * </p>
     *
     * @param id 订单唯一标识
     * @return 订单详情对象
     * @throws BusinessException 订单不存在时抛出
     */
    @Override
    public RepairOrderVO getOrderById(Long id) {
        log.debug("查询报修单，id：{}",id);
        RepairOrder order=repairOrderDao.findById(id);
        if(order==null){
            log.warn("报修单不存在,id：{}",id);
            throw new BusinessException("报修单不存在");
        }
        return convertToVO(order);
    }
    /**
     * 查询所有维修订单的实现逻辑（仅管理员可用）
     * <p>
     * 简单转发调用DAO层查询全量订单，无额外业务逻辑，日志级别DEBUG（调试用）
     * </p>
     *
     * @return 系统中所有维修订单列表，无订单时返回空列表
     * @throws BusinessException 非管理员角色调用时由DAO层抛出权限异常
     */
    @Override
    public List<RepairListVO> getAllOrders() {
        log.debug("查询所有报修单");
        List<RepairOrder> orders= repairOrderDao.selectAll();
        List<RepairListVO> vos=new ArrayList<>();
        for (RepairOrder order:orders){
            vos.add(convertToListVO(order));
        }
        return vos;
    }
    /**
     * 按状态筛选订单的实现逻辑（仅管理员可用）
     * <p>
     * 核心逻辑：调用DAO层按状态筛选订单，状态码通过枚举转换为中文描述便于日志查看
     * </p>
     *
     * @param status 订单状态（1-待维修 2-已完成 3-已取消）
     * @return 指定状态的订单列表，无符合条件订单时返回空列表
     */
    @Override
    public List<RepairListVO> getOrdersByStatus(Character status) {
        log.debug("查询表单，状态:{}",RepairOrderStatus.getStatus( status));
        List<RepairOrder> orders= repairOrderDao.selectByStatus(status);
        List<RepairListVO> vos=new ArrayList<>();
        for (RepairOrder order:orders){
            vos.add(convertToListVO(order));
        }
        return vos;
    }
    /**
     * 修改订单状态的实现逻辑（仅管理员可用）
     * <p>
     * 核心步骤：
     * 1. 校验订单是否存在（不存在则抛业务异常，日志级别WARN）；
     * 2. 更新订单状态和更新时间；
     * 3. 提交更新（受事务控制），更新失败则抛业务异常（日志级别ERROR）
     * </p>
     * @param id 订单ID
     * @param status 新状态（1-待维修 2-已完成 3-已取消）
     * @throws BusinessException 订单不存在/更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(Long id, Character status) {
        log.info("更新报修单状态，id:{},状态:{}",id,RepairOrderStatus.getStatus(status));
        //1. 校验订单是否存在
        RepairOrder order=repairOrderDao.findById(id);
        if(order==null){
            log.warn("报修单不存在,id:{}",id);
            throw new BusinessException("报修单不存在");
        }
        //2. 更新订单状态和更新时间
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        int result=repairOrderDao.update(order);
        //3. 提交更新（受事务控制），更新失败则抛业务异常（日志级别ERROR）
        if(result<=0){
            log.error("更新报修单状态失败,id:{}",id);
            throw new BusinessException("更新报修单状态失败");
        }
        log.info("更新报修单状态成功,id:{}",id);
        String deviceType= repairOrderDao.getDeviceType(id);
        String statusName=RepairOrderStatus.getStatus(status);
        String deviceTypeName= DeviceType.getDeviceName(deviceType.charAt(0));
        messageService.sendMessage(order.getStudentAccount(),"报修单状态更新",
                "您的报修单"+statusName+"\n设备类型："+deviceTypeName,
                 '2', id);
        log.info("发送报修单状态更新消息成功,学生账号:{}",order.getStudentAccount());
    }
    /**
     * 取消订单的实现逻辑（仅学生可用，且订单为待维修状态）
     * <p>
     * 核心步骤：
     * 1. 校验订单是否存在（不存在则抛业务异常，日志级别WARN）；
     * 2. 将订单状态更新为「已取消」；
     * 3. 提交更新（受事务控制），更新失败则抛业务异常（日志级别ERROR）
     * </p>
     * @param id 订单ID
     * @throws BusinessException 订单不存在/取消失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id) {
        log.info("取消报修单,ID:{}",id);
        //1. 校验订单是否存在（不存在则抛业务异常，日志级别WARN）；
        RepairOrder order=repairOrderDao.findById(id);
        if(order==null){
            log.warn("报修单不存在,id:{}",id);
            throw new BusinessException("报修单不存在");
        }
        //2. 将订单状态更新为「已取消」；
        order.setStatus(RepairOrderStatus.CANCELED.getCode());
        int result=repairOrderDao.update( order);
        //3. 提交更新（受事务控制），更新失败则抛业务异常（日志级别ERROR）
        if(result<=0){
            log.error("取消报修单失败,id:{}",id);
            throw new BusinessException("取消报修单失败");
        }
        log.info("取消报修单成功,id:{}",id);
    }
    /**
     * 删除订单的实现逻辑（仅管理员可用，物理删除）
     * <p>
     * 核心步骤：
     * 1. 校验订单是否存在（不存在则抛业务异常，日志级别WARN）；
     * 2. 删除订单数据（受事务控制）；
     * 3. 删除失败则抛业务异常（日志级别ERROR）
     * </p>
     * @param id 订单ID
     * @throws BusinessException 订单不存在/删除失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        log.info("删除报修单,ID:{}",id);
        // 1. 校验订单是否存在（不存在则抛业务异常，日志级别WARN）；
        RepairOrder order=repairOrderDao.findById(id);
        if(order==null){
            log.warn("报修单不存在,id:{}",id);
            throw new BusinessException("报修单不存在");
        }
        // 2. 删除订单数据（受事务控制）；
        boolean result=repairOrderDao.deleteById(id);
        //3. 删除失败则抛业务异常（日志级别ERROR）
        if(!result){
            log.error("删除报修单失败,id:{}",id);
            throw new BusinessException("删除报修单失败");
        }
    }

    /**
     * 按楼栋查询订单的实现逻辑
     *
     * @param dormBuilding
     * @return
     */
    @Override
    public List<RepairListVO> getOrdersByDorm(String dormBuilding) {
        log.debug("查询楼栋报修单，楼栋：{}", dormBuilding);
        List<RepairOrder> orders = repairOrderDao.selectByDormBuilding(dormBuilding);
        List<RepairListVO> vos = new ArrayList<>();
        for (RepairOrder order : orders) {
            vos.add(convertToListVO(order));
        }
        return vos;
    }

    private RepairListVO convertToListVO(RepairOrder order) {
        if (order == null) {
            return null;
        }
        RepairListVO vo = new RepairListVO();
        vo.setId(order.getId());
        vo.setDeviceType(order.getDeviceType());
        vo.setStatus(order.getStatus());
        vo.setPriority(order.getPriority());
        vo.setDormBuilding(order.getDormBuilding());
        vo.setDormRoom(order.getDormRoom());
        vo.setCreateTime(order.getCreateTime());
        return vo;
    }

    /**
     * Entity 转详情 VO
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
        return vo;
    }
    public PageResult<RepairListVO> queryOrders(OrderQueryDTO queryDTO) {
        log.info("多条件查询报修单，条件：{}", queryDTO);

        // 计算分页偏移量
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        queryDTO.setPageNum(offset);

        // 查询列表
        List<RepairListVO> list = repairOrderDao.selectByCondition(queryDTO);

        // 查询总数
        Long total = repairOrderDao.countByCondition(queryDTO);

        // 返回分页结果
        return new PageResult<>(list, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }
}
