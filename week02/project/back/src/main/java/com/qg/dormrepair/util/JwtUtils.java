package com.qg.dormrepair.util;

import com.qg.dormrepair.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * JWT工具类
 * 支持双Token模式（AccessToken+RefreshToken）
 * 可存储用户身份、角色、权限信息，用于登录认证与权限校验
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {
    /**
     * JWT配置信息（密钥、过期时间）
     */
    private final JwtProperties jwtProperties;

    /**
     * 生成AccessToken（携带用户权限列表，用于接口鉴权）
     *
     * @param account 用户账号
     * @param role 用户角色
     * @param permissions 权限集合
     * @return 加密后的AccessToken
     */
    public String generateAccessToken(String account, String role, Set<String> permissions) {
        return generateToken(account, role, permissions, jwtProperties.getExpire(), "access");
    }

    /**
     * 生成RefreshToken（仅用于刷新AccessToken，不存储权限）
     *
     * @param account 用户账号
     * @param role 用户角色
     * @return 加密后的RefreshToken
     */
    public String generateRefreshToken(String account, String role) {
        return generateToken(account, role, null, jwtProperties.getRefreshExpire(), "refresh");
    }

    /**
     * 统一Token生成方法
     *
     * @param account 用户账号
     * @param role 用户角色
     * @param permissions 权限集合
     * @param expire 过期时间
     * @param type token类型（access/refresh）
     * @return 最终JWT字符串
     */
    private String generateToken(String account, String role, Set<String> permissions,
                                 Long expire, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("account", account);
        claims.put("role", role);
        claims.put("type", type);

        // 只有AccessToken才存入权限列表
        if ("access".equals(type) && permissions != null && !permissions.isEmpty()) {
            claims.put("permissions", permissions);
        }

        long now = System.currentTimeMillis();
        Date expiration = new Date(now + expire);

        log.info("生成【{}】Token → 账号：{}，过期时间：{}", type, account, expiration);

        return Jwts.builder()
                .addClaims(claims)
                .setExpiration(expiration)
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSignKey())
                .compact();
    }

    /**
     * 解析Token，获取载荷信息
     *
     * @param token JWT字符串
     * @return Claims载荷对象
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtProperties.getSignKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Token 解析失败", e);
            return null;
        }
    }

    /**
     * 从Token中获取权限集合（兼容List/Set类型）
     *
     * @param claims 已解析的Token载荷
     * @return 权限集合
     */
    public Set<String> getPermissionsFromToken(Claims claims) {
        try {
            log.error("===== 完整 claims：{} =====", claims);
            Object obj = claims.get("permissions");
            log.error("===== permissions 原始值：{} =====", obj);

            // 兼容处理：统一转为Set<String>
            if (obj instanceof Iterable<?>) {
                Set<String> result = new HashSet<>();
                for (Object item : (Iterable<?>) obj) {
                    result.add(item.toString());
                }
                return result;
            }

            return null;
        } catch (Exception e) {
            log.error("获取权限失败", e);
            return null;
        }
    }

    /**
     * 判断Token是否已过期
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return true;
        return claims.getExpiration().before(new Date());
    }

    /**
     * 获取Token类型（access/refresh）
     */
    public String getTypeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get("type", String.class);
    }

    /**
     * 从Token中获取用户账号
     */
    public String getAccountFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get("account", String.class);
    }

    /**
     * 从Token中获取用户角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims == null ? null : claims.get("role", String.class);
    }
    /**
     * 获取 Token 剩余过期时间（秒）
     * 专门给 Redis 黑名单自动过期用
     */
    public long getExpireFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return -1;
            }
            long expireTime = claims.getExpiration().getTime();
            long now = System.currentTimeMillis();
            return (expireTime - now) / 1000;
        } catch (Exception e) {
            log.error("获取Token剩余过期时间失败", e);
            return -1;
        }
    }

}