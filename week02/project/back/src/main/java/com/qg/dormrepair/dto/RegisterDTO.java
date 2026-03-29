package com.qg.dormrepair.dto;

import com.qg.dormrepair.constants.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 注册请求DTO(数据传输对象)
 * 用于接收前端注册的请求，包括账号，密码，角色三个核心字段
 * 规则与{@link RegexConstants}全局正则常量保持统一
 */
@Data
@Schema(description = "用户注册请求参数")
public class RegisterDTO {

    /**
     * 注册账号
     * <p>
     * 业务说明：用户注册的唯一标识，支持学生学号（3125/3225开头）或管理员ID（0025开头）<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 格式需匹配{@link RegexConstants#USER_ID}正则规则（10位数字，前缀为3125/3225/0025，后接6位数字）；
     * 示例：3125012345（学生账号）
     * </p>
     */
    @Schema(description = "账号（学号/管理员ID）", required = true, example = "3125004123")
    @NotBlank(message = "账号不能为空")
    @Pattern(regexp = RegexConstants.STUDENT_ID, message = "密码格式错误")
    private String account;

    /**
     * 注册密码
     * <p>
     * 业务说明：用户注册后登录系统的密码，区分大小写<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 格式需匹配{@link RegexConstants#PASSWORD}正则规则（6-10位，仅包含大小写字母和数字，无特殊字符）；
     * 示例：Abc123、1234567890、Qwe7890
     * </p>
     */
    @Schema(description = "密码（6-10位英文数字）", required = true, example = "123456")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = RegexConstants.PASSWORD, message = "密码格式错误(6-10位英文数字)")
    private String pwd;

    /**
     * 用户角色
     * <p>
     * 业务说明：标识注册用户的身份类型，决定后续系统操作权限<br>
     * 校验规则：
     * 1. 非空（不能为null、空字符串或全空格）；
     * 2. 格式需匹配{@link RegexConstants#ROLE}正则规则（仅支持1、2两个数字）；
     * 角色含义：
     * - 1：学生（可提交报修订单、查看自己的订单状态）；
     * - 2：管理员（可处理报修订单、修改订单状态、管理用户信息）
     * </p>
     */
    @Schema(description = "角色 1-学生 2-管理员", required = true, example = "1")
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "1", message = "角色选择错误（1-学生 2-管理员）")
    private String role;
}