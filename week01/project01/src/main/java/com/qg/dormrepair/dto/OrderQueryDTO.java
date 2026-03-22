// dto/OrderQueryDTO.java
package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 报修单多条件查询 DTO
 */
@Data
public class OrderQueryDTO {
    // 可选：按状态查询
    @Pattern(regexp = RegexConstants.ORDER_STATUS, message = "状态参数错误")
    private String status;
    // 可选：按宿舍楼栋查询
    private String dormBuilding;
    // 可选：按时间查询
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    // 可选：按优先级查询
    @Pattern(regexp = RegexConstants.PRIORITY, message = "优先级参数错误")
    private Character priority;

    // 可选：按设备类型查询
    @Pattern(regexp = RegexConstants.DEVICE_TYPE, message = "设备类型参数错误")
    private Character deviceType;

    // 可选：按学生账号查询
    @Pattern(regexp = RegexConstants.STUDENT_ID, message = "学生账号参数错误")
    private String studentAccount;

    // 分页参数,默认1，10
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
