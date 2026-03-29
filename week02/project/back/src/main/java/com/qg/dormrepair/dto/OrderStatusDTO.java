package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 维修订单状态修改请求DTO（数据传输对象）
 * <p>
 * 用于接收前端提交的订单状态变更参数，仅包含订单状态字段，
 * 校验规则与{@link RegexConstants#ORDER_STATUS}全局正则常量保持一致
 * </p>
 */
@Data
@Schema(description = "订单状态修改请求参数")
public class OrderStatusDTO {

    /**
     * 订单状态
     * <p>
     * 业务说明：标识订单处理状态（如1=待处理、2=已完成、3=已取消）<br>
     * 校验规则：
     * 1. 非空（不能为null）；
     * 2. 匹配{@link RegexConstants#ORDER_STATUS}全局正则常量
     * </p>
     */
    @Schema(description = "订单状态 1-待维修 2-已完成 3-已取消", required = true, example = "1")
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = RegexConstants.ORDER_STATUS, message = "状态格式错误:1-待维修 2-已完成 3-已取消")
    private String status;
}