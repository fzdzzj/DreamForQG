package com.qg.dormrepair.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * 报修单
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairOrder {
    //报修单 ID
    private Long id;
    //宿舍楼栋
    private String dormBuilding;
    private String dormRoom;
    //学生账号
    private String studentAccount;
    //设备类型
    private Character deviceType;
    //描述
    private String description;
    //创建时间
    private LocalDateTime createTime;
    //更新时间
    private LocalDateTime updateTime;
    //优先级
    private Character priority;
    //状态
    private Character status;
    @Override
    public String toString() {
        return "RepairOrder{" +
                "id=" + id +
                ", studentAccount='" + studentAccount + '\'' +
                ", deviceType=" + deviceType +
                ", description='" + description + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", priority=" + priority +
                ", status=" + status +
                '}';
    }
}
