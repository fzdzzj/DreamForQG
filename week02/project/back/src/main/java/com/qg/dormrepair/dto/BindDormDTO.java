package com.qg.dormrepair.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 宿舍绑定DTO
 */
@Data
@Schema(description = "宿舍绑定请求参数")
public class BindDormDTO {

    @Schema(description = "宿舍楼栋", required = true, example = "A栋")
    @NotBlank(message="请选择宿舍楼栋")
    private String dormBuilding;

    @Schema(description = "宿舍房间号", required = true, example = "101")
    @NotBlank(message="请输入房间号")
    private String dormRoom;
}