package com.qg.dormrepair.mapper;

import com.qg.dormrepair.dto.OrderQueryDTO;
import com.qg.dormrepair.pojo.RepairOrder;
import com.qg.dormrepair.vo.RepairListVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface RepairOrderDao {
    @Select("SELECT device_type FROM repair_order WHERE id = #{id}")
    String getDeviceType(Long id);

    // 列表查询 - 只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order WHERE student_account = #{account}")
    List<RepairOrder> selectListByAccount(String account);

    // 全部查询 - 管理员用,只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order")
    List<RepairOrder> selectAll();

    // 详情查询 - 查全部字段
    @Select("SELECT * FROM repair_order WHERE id = #{id}")
    RepairOrder findById(Long id);

    // 按状态查询- 只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order WHERE status = #{status}")
    List<RepairOrder> selectByStatus(Character status);

    // 按楼栋查询- 只查必要字段
    @Select("SELECT id, device_type, description, status, priority, " +
            "dorm_building, dorm_room, create_time " +
            "FROM repair_order WHERE dorm_building = #{dormBuilding}")
    List<RepairOrder> selectByDormBuilding(String dormBuilding);

    //  插入 - 需要完整 Entity
    @Insert("INSERT INTO repair_order (student_account, dorm_building, dorm_room, " +
            "device_type, description, status, priority, create_time, update_time) " +
            "VALUES (#{studentAccount}, #{dormBuilding}, #{dormRoom}, " +
            "#{deviceType}, #{description}, #{status}, #{priority}, " +
            "#{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    boolean insert(RepairOrder repairOrder);

    // 更新 - 需要完整 Entity
    int update(RepairOrder repairOrder);

    //  删除
    @Delete("DELETE FROM repair_order WHERE id = #{id}")
    boolean deleteById(Long id);

    //  多条件查询
    List<RepairListVO> selectByCondition(OrderQueryDTO queryDTO);

    // ✅ 统计总数
    Long countByCondition(OrderQueryDTO queryDTO);
}
