package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
/**
 * 宿舍信息DTO（数据传输对象）
 * <p>
 * 用于接收和传递宿舍楼栋、房间号等核心信息，
 * </p>
 */
@Data
public class DormDTO {
    /**
     * 宿舍楼栋
     * <p>
     * 业务说明：标识宿舍所在楼栋，如5栋、6栋等<br>
     * 校验规则：非空（不能为null、空字符串或全空格）
     * </p>
     */
    @NotBlank(message = "宿舍楼栋不能为空")
    private String dormBuilding;

    /**
     * 宿舍房间
     * <p>
     * 业务说明：标识宿舍房间号，如101、102、103等<br>
     * 校验规则：非空（不能为null、空字符串或全空格）
     * </p>
     */
    @NotBlank(message = "宿舍房间不能为空")
    private String dormRoom;
}
