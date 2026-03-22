package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
/**
 * 登录请求DTO（数据传输对象）
 * <p>
 * 用于接收前端提交的登录请求参数，包含账号、密码核心字段，
 * 规则与{@link RegexConstants}全局正则常量保持统一
 * </p>
 */
@Data
public class LoginDTO {
    /**
     * 账号
     * <p>
     * 业务说明：用户登录账号，可以是学号或管理员ID<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 匹配{@link RegexConstants#USER_ID}正则规则（学号或管理员ID）
     * </p>
     */
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp= RegexConstants.USER_ID,message = "账号格式错误")
    private String account;

    /**
     * 密码
     * <p>
     * 业务说明：用户登录密码<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 匹配{@link RegexConstants#PASSWORD}正则规则（6-10位英文数字）
     * </p>
     */
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp="[a-zA-Z0-9]{6,10}",message = "密码格式错误(6-10位英文数字)")
    private String pwd;
}
