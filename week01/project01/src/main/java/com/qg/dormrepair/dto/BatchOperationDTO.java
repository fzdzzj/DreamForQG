// dto/BatchOperationDTO.java
package com.qg.dormrepair.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * 批量操作 DTO
 */
@Data
public class BatchOperationDTO {
    @NotEmpty(message = "请选择要操作的消息")
    @Size(max = 100, message = "一次最多操作 100 条消息")
    private List<Long> messageIds;
}
