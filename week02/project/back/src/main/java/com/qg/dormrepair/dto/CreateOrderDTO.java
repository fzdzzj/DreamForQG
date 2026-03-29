package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

/**
 * 报修订单创建请求DTO（数据传输对象）
 * 用于接收前端提交的宿舍维修订单创建请求参数，封装设备类型、问题描述、优先级等核心信息，
 * 从RegexConstants类中定义的正则表达式进行参数校验
 */
@Data
@Schema(description = "创建报修单请求参数")
public class CreateOrderDTO {

    @Schema(description = "宿舍楼栋", required = true, example = "A栋")
    @NotBlank(message = "宿舍楼栋不能为空")
    String dormBuilding;

    @Schema(description = "宿舍房间号", required = true, example = "101")
    @NotBlank(message = "宿舍号不能为空")
    String dormRoom;

    /**
     * 设备类型
     * <p>
     * 业务说明：标识需要维修的设备种类<br>
     * 校验规则：
     * 1. 非空（不能为null）；
     * 2. 格式需匹配{@link RegexConstants#DEVICE_TYPE}正则规则（1-9的单个数字）
     * </p>
     */
    @Schema(description = "设备类型 1-水龙头 2-马桶 3-电灯 4-床 5-门 6-水槽 7-电表 8-水表", required = true, example = "1")
    @NotNull(message = "设备类型不能为空")
    @Pattern(regexp = RegexConstants.DEVICE_TYPE, message = "设备类型格式错误")
    private String deviceType;

    /**
     * 问题描述
     * <p>
     * 业务说明：详细描述设备故障情况，便于维修人员了解问题（如「3栋201室空调不制冷」「5栋305室水龙头漏水」）<br>
     * 校验规则：非空（不能为null、空字符串或全空格）
     * </p>
     */
    @Schema(description = "问题描述", required = true, example = "水龙头漏水严重")
    @NotBlank(message = "问题描述不能为空")
    private String description;

    /**
     * 报修优先级
     * <p>
     * 业务说明：标识维修请求的紧急程度，用于维修人员排序处理<br>
     * 校验规则：
     * 1. 非空（不能为null）；
     * 2. 格式需匹配{@link RegexConstants#PRIORITY}正则规则（1-3的单个数字）；
     * 优先级含义：1=普通、2=紧急、3=非常紧急
     * </p>
     */
    @Schema(description = "报修优先级 1-普通 2-紧急 3-非常紧急", required = true, example = "1")
    @NotNull(message = "优先级不能为空")
    @Pattern(regexp = RegexConstants.PRIORITY, message = "优先级格式错误（1-普通，2-紧急，3-非常紧急）")
    private String priority;

    //新增：图片列表（非必传）
    @Schema(description = "图片URL列表", example = "[\"https://xxx.com/1.jpg\"]")
    private List<String> images;
}