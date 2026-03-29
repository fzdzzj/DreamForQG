package com.qg.dormrepair.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息响应体")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户账号（学号/管理员ID）", example = "3125004123")
    private String account;

    @Schema(description = "用户角色 1-学生 2-管理员", example = "1")
    private String role;

    @Schema(description = "宿舍楼栋", example = "A栋")
    private String dormBuilding;

    @Schema(description = "宿舍房间号", example = "101")
    private String dormRoom;

    //不要包含pwd，防止敏感信息泄露
}