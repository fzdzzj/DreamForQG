package com.qg.dormrepair.mapper;

import com.qg.dormrepair.annotation.AutoFill;
import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.enums.DataBaseOperationType;
import com.qg.dormrepair.enums.RepairOrderStatus;
import com.qg.dormrepair.pojo.RepairOrder;
import com.qg.dormrepair.vo.RepairListVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修单持久层接口
 */
public interface RepairOrderDao {
    @Select("SELECT device_type FROM repair_order WHERE id = #{id}")
    String getDeviceType(Long id);

    // 列表查询 - 只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order WHERE student_account = #{account} order by create_time desc")
    List<RepairOrder> selectListByAccount(String account);

    // 全部查询 - 管理员用,只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order order by create_time desc ")
    List<RepairOrder> selectAll();

    // 详情查询 - 查全部字段
    @Select("SELECT * FROM repair_order WHERE id = #{id}")
    RepairOrder findById(Long id);

    // 按状态查询- 只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order WHERE status = #{status} order by create_time desc")
    List<RepairOrder> selectByStatus(String status);

    // 按楼栋查询- 只查必要字段（终极版：自动忽略所有空格）
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order WHERE REPLACE(dorm_building, ' ', '') LIKE CONCAT('%', REPLACE(#{dormBuilding}, ' ', ''), '%') order by create_time desc")
    List<RepairOrder> selectByDormBuilding(String dormBuilding);






    @Insert("INSERT INTO repair_order (student_account, dorm_building, dorm_room, " +
            "device_type, description, status, priority, create_time, update_time, images) " +
            "VALUES (#{studentAccount}, #{dormBuilding}, #{dormRoom}, " +
            "#{deviceType}, #{description}, #{status}, #{priority}, " +
            "#{createTime}, #{updateTime}, #{images})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @AutoFill(DataBaseOperationType.INSERT)
    boolean insert(RepairOrder repairOrder);
    // 更新 - 需要完整 Entity
    @AutoFill(DataBaseOperationType.UPDATE)
    int update(RepairOrder repairOrder);

    //  删除
    @Delete("DELETE FROM repair_order WHERE id = #{id}")
    boolean deleteById(Long id);

    //  多条件查询
    List<RepairListVO> selectByCondition(@Param("queryDTO") OrderQueryDTO queryDTO,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

    // 统计总数
    Long countByCondition(@Param("queryDTO") OrderQueryDTO queryDTO);

    @Select("SELECT id FROM repair_order WHERE id = #{id} AND student_account = #{account}")
    Long isOrderBelongToUser(Long id, String account);

    @Select("SELECT id FROM repair_order WHERE status = #{repairOrderStatus} AND create_time <= #{time} AND priority = #{priority}")
    List<Long> getOrdersByStatusAndTimeAndPriority(String repairOrderStatus, LocalDateTime time,String  priority);
}
