package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 密码修改请求DTO（数据传输对象）
 * <p>
 * 用于接收前端提交的密码修改请求参数，包含旧密码、新密码两个核心字段，
 * 新密码校验规则与{@link RegexConstants#PASSWORD}全局正则常量保持一致
 * </p>
 */
@Data
@Schema(description = "密码修改请求参数")
public class PasswordDTO {

    /**
     * 旧密码
     * <p>
     * 业务说明：用户当前的登录密码，用于验证身份合法性<br>
     * 校验规则：非空（不能为null、空字符串或全空格），长度在6-10位之间，只允许英文字母、数字
     * </p>
     */
    @Schema(description = "旧密码（6-10位英文数字）", required = true, example = "123456")
    @NotBlank(message = "旧密码不能为空")
    @Pattern(regexp = RegexConstants.PASSWORD, message = "密码格式错误(6-10位英文数字)")
    private String oldPwd;

    /**
     * 新密码
     * <p>
     * 业务说明：用户新设置的密码，用于更新密码<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 符合{@link RegexConstants#PASSWORD}全局正则表达式定义的格式
     * </p>
     */
    @Schema(description = "新密码（6-10位英文数字）", required = true, example = "654321")
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = RegexConstants.PASSWORD, message = "密码格式错误(6-10位英文数字)")
    private String newPwd;
}