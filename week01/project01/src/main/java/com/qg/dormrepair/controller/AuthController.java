package com.qg.dormrepair.controller;

import com.qg.dormrepair.dto.LoginDTO;
import com.qg.dormrepair.dto.PasswordDTO;
import com.qg.dormrepair.dto.RegisterDTO;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.JwtUtils;
import com.qg.dormrepair.util.PasswordUtil;
import com.qg.dormrepair.util.RegexUtil;
import com.qg.dormrepair.vo.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    /**
     * 用户登录接口
     * <p>
     * 验证用户账号和密码的合法性，登录成功后生成JWT令牌并返回，
     * 入参需通过校验确保账号/密码格式符合规则
     * </p>
     * @param loginDTO 登录请求DTO（请求体），包含账号（学生/管理员ID）和密码
     * @return 统一响应结果，数据体为登录响应VO（包含JWT令牌、用户角色等信息）
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Validated @RequestBody LoginDTO loginDTO){
        log.info("用户登录尝试,账号{}", loginDTO.getAccount());
        LoginResponse response = userService.login(loginDTO);
        return Result.success(response);
    }
    /**
     * 用户注册接口
     * <p>
     * 新增学生/管理员账号，入参需通过校验确保账号/密码/角色格式合法，
     * 注册时会对密码进行加密处理（非明文存储），并校验账号唯一性（避免重复注册）
     * </p>
     * @param registerDTO 注册请求DTO（请求体），包含账号、密码、角色
     * @return 统一响应结果，无返回数据
     */
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO){
        log.info("用户注册,账号{}", registerDTO.getAccount());
        userService.register(registerDTO);
        return Result.success();
    }
    @PutMapping("/password")
    public Result<String> updatePassword(@Valid @RequestBody PasswordDTO passwordDTO){
        log.info("用户修改密码,账号{}", CurrentHolder.getCurrentUser().getAccount());
        String token=userService.updatePassword(passwordDTO.getOldPwd(), passwordDTO.getNewPwd());
        return Result.success(token);
    }

}
