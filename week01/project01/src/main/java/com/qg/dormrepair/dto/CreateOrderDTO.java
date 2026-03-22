package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
/**
 * 报修订单创建请求DTO（数据传输对象）
 * 用于接收前端提交的宿舍维修订单创建请求参数，封装设备类型、问题描述、优先级等核心信息，
 *从RegexConstants类中定义的正则表达式进行参数校验
 */
@Data
public class CreateOrderDTO {
    /**
     * 设备类型
     * <p>
     * 业务说明：标识需要维修的设备种类（如1=空调、2=水龙头、3=电灯、4=门锁等）<br>
     * 校验规则：
     * 1. 非空（不能为null）；
     * 2. 格式需匹配{@link RegexConstants#DEVICE_TYPE}正则规则（1-9的单个数字）
     * </p>
     */
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
    @NotBlank(message = "问题描述不能为空")
    private String description;

    /**
     * 报修优先级
     * <p>
     * 业务说明：标识维修请求的紧急程度，用于维修人员排序处理<br>
     * 校验规则：
     * 1. 非空（不能为null）；
     * 2. 格式需匹配{@link RegexConstants#}正则规则（1-3的单个数字）；
     * 优先级含义：1=普通、2=紧急、3=非常紧急
     * </p>
     */
    @NotNull(message = "优先级不能为空")
    @Pattern(regexp = RegexConstants.PRIORITY, message = "优先级格式错误（1-普通，2-紧急，3-非常紧急）")
    private String priority;
}
