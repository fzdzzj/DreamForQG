// dto/BatchOperationDTO.java
package com.qg.dormrepair.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量操作 DTO
 */
@Data
@Schema(description = "批量消息操作请求参数")
public class BatchMessageDTO {

    @Schema(description = "消息ID列表", required = true, example = "[1001,1002,1003]")
    @NotEmpty(message = "请选择要操作的消息")
    @Size(max = 100, message = "一次最多操作 100 条消息")
    private List<Long> messageIds;
}