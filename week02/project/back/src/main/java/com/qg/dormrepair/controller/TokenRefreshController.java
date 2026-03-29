package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.impl.TokenRefreshServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Token刷新接口", description = "Token刷新、用户退出登录")
@CrossOrigin(origins = "*")
public class TokenRefreshController {
    private final TokenRefreshServiceImpl tokenRefreshService;

    /**
     * 刷新 Token 接口
     * 前端传入：refreshToken
     * 返回：【只返回新 accessToken】
     */
    @PostMapping("/refresh")
    @OperationLog("刷新Token")
    @Operation(summary = "刷新AccessToken", description = "使用refreshToken获取新的accessToken")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            log.error("Refresh Token 不能为空");
            return Result.error(400, "Refresh Token 不能为空");
        }

        log.info("刷新Token, Refresh Token:{}", refreshToken);
        Map<String, String> newTokens = tokenRefreshService.refreshTokens(refreshToken);
        log.info("刷新Token成功");
        return Result.success(newTokens);
    }

    /**
     * 退出登录
     * 【正确做法：同时拉黑 accessToken + refreshToken】
     */
    @OperationLog("退出登录")
    @PostMapping("/logout")
    @Operation(summary = "用户退出登录", description = "拉黑accessToken和refreshToken，实现安全登出")
    public Result<Void> logout(
            @Parameter(description = "Bearer token", required = true, example = "Bearer eyJhbGciOiJ...")
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody(required = false) Map<String, String> requestBody) {

        // 1. 获取 accessToken
        String accessToken = null;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        }

        // 2. 获取 refreshToken（前端必须传过来）
        String refreshToken = null;
        if (requestBody != null) {
            refreshToken = requestBody.get("refreshToken");
        }else if(refreshToken == null){
            throw new BusinessException(400,"refreshToken不能为空");
        }

        // 3. 两个 Token 都拉黑（关键！）
        if (accessToken != null) {
            tokenRefreshService.addTokenToBlacklist(accessToken);
        }
        if (refreshToken != null) {
            tokenRefreshService.addTokenToBlacklist(refreshToken);
        }

        log.info("用户退出登录成功，双Token已拉黑");
        return Result.success();
    }
}