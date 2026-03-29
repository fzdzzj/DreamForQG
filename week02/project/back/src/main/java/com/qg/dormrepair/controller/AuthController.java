package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.dto.LoginDTO;
import com.qg.dormrepair.dto.PasswordDTO;
import com.qg.dormrepair.dto.RegisterDTO;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.util.CurrentHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "用户认证接口", description = "用户登录、注册、密码修改")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;

    /**
     * 用户登录接口
     * <p>
     * 验证用户账号和密码的合法性，登录成功后生成JWT令牌并返回，
     * 入参需通过校验确保账号/密码格式符合规则
     * </p>
     *
     * @param loginDTO 登录请求DTO（请求体），包含账号（学生/管理员ID）和密码
     * @return 统一响应结果，数据体为登录响应VO（包含JWT令牌、用户角色等信息）
     */
    @PostMapping("/login")
    @OperationLog("用户登录")
    @Operation(summary = "用户登录", description = "学生/管理员通用登录，返回JWT令牌")
    public Result<Map<String, Object>> login(@Validated @RequestBody LoginDTO loginDTO) {
        log.info("用户登录尝试,账号{}", loginDTO.getAccount());
        Map<String, Object> response = userService.login(loginDTO);
        log.info("用户登录成功,账号{}", loginDTO.getAccount());
        return Result.success(response);
    }

    /**
     * 用户注册接口
     * <p>
     * 新增学生/管理员账号，入参需通过校验确保账号/密码/角色格式合法，
     * 注册时会对密码进行加密处理（非明文存储），并校验账号唯一性（避免重复注册）
     * </p>
     *
     * @param registerDTO 注册请求DTO（请求体），包含账号、密码、角色
     * @return 统一响应结果，无返回数据
     */
    @PostMapping("/register")
    @OperationLog("用户注册")
    @Operation(summary = "用户注册", description = "新用户注册，支持学生/管理员角色")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册,账号{}", registerDTO.getAccount());
        userService.register(registerDTO);
        log.info("用户注册成功,账号{}", registerDTO.getAccount());
        return Result.success();
    }

    /**
     * 修改密码
     * <p>
     * 修改当前登录用户的密码，入参需通过校验确保密码格式合法
     * </p>
     *
     * @param passwordDTO 修改密码请求DTO（请求体），包含旧密码和新密码
     * @return 统一响应结果，无返回数据
     */
    @OperationLog("用户修改密码")
    @PutMapping("/update-password")
    @Operation(summary = "用户修改密码", description = "需登录，验证旧密码后更新为新密码")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        log.info("用户修改密码,账号{}", CurrentHolder.getCurrentUser().getAccount());
        userService.updatePassword(passwordDTO.getOldPwd(), passwordDTO.getNewPwd());
        log.info("用户修改密码成功,账号{}", CurrentHolder.getCurrentUser().getAccount());
        return Result.success();
    }

}