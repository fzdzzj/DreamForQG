package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.constants.MessageConstant;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.service.PermissionService;
import com.qg.dormrepair.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Token 刷新服务实现类（无泛型版）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshServiceImpl {

    private final JwtUtils jwtUtils;
    private final PermissionService permissionService;

    private final RedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX = "token:blacklist:";

    /**
     * 刷新 AccessToken
     */
    public Map<String, String> refreshTokens(String refreshToken) {
        log.info("开始执行 AccessToken 刷新操作");

        if (refreshToken == null || refreshToken.isBlank()) {
            log.error("刷新 Token 失败：" + MessageConstant.REFRESH_TOKEN_NOT_EMPTY);
            throw new BusinessException(401, MessageConstant.REFRESH_TOKEN_NOT_EMPTY);
        }

        // Redis 黑名单校验
        String blackKey = BLACKLIST_PREFIX + refreshToken;
        Boolean hasKey = redisTemplate.hasKey(blackKey);
        if (Boolean.TRUE.equals(hasKey)) {
            log.warn("RefreshToken 已被拉黑：{}", refreshToken);
            throw new BusinessException(401, MessageConstant.REFRESH_TOKEN_INVALID + "，请重新登录");
        }
        log.info("RefreshToken 黑名单校验通过");

        // 过期校验
        if (jwtUtils.isTokenExpired(refreshToken)) {
            log.warn("RefreshToken 已过期：{}", refreshToken);
            throw new BusinessException(401, MessageConstant.REFRESH_TOKEN_EXPIRED + "，请重新登录");
        }

        // Token 类型必须是 refresh
        String type = jwtUtils.getTypeFromToken(refreshToken);
        if (!"refresh".equals(type)) {
            log.error("Token 类型非法：{}", type);
            throw new BusinessException(401, MessageConstant.TOKEN_TYPE_ILLEGAL);
        }

        // 解析用户信息
        String account = jwtUtils.getAccountFromToken(refreshToken);
        String role = jwtUtils.getRoleFromToken(refreshToken);
        if (account == null || role == null) {
            throw new BusinessException(401, MessageConstant.TOKEN_INVALID);
        }

        // 查询最新权限
        Set<String> permissions = permissionService.getPermissionsByRole(role);

        // 生成新 AccessToken
        String newAccessToken = jwtUtils.generateAccessToken(account, role, permissions);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        return tokens;
    }

    /**
     * 加入黑名单
     */
    public void addTokenToBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            long expireSeconds = jwtUtils.getExpireFromToken(token);
            if (expireSeconds > 0) {
                String key = BLACKLIST_PREFIX + token;
                redisTemplate.opsForValue().set(key, "1", expireSeconds, TimeUnit.SECONDS);
                log.info("Token 已加入黑名单，剩余有效期：{}秒", expireSeconds);
            }
        } catch (Exception e) {
            log.error("Token 加入黑名单异常", e);
        }
    }

    /**
     * 判断是否在黑名单
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}