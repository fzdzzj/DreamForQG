// dto/OrderQueryDTO.java
package com.qg.dormrepair.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报修单多条件查询 DTO
 */
@Data
public class OrderQueryDTO {

    //  按状态查询（1-待维修 2-已完成 3-已取消）
    private Character status;

    // 按楼栋查询（A 栋/B 栋/C 栋）
    private String dormBuilding;

    //  按时间范围查询
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // 可选：按优先级查询
    private Character priority;

    // 可选：按设备类型查询
    private Character deviceType;

    // 可选：按学生账号查询
    private String studentAccount;

    // 分页参数,默认1，10
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
