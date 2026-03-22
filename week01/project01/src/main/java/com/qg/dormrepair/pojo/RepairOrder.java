package com.qg.dormrepair.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairOrder {
    private Long id;
    private String dormBuilding;
    private String dormRoom;
    private String studentAccount;
    private Character deviceType;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Character priority;
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
