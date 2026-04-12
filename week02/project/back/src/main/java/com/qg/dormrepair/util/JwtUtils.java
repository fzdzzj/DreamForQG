package com.qg.dormrepair.util;

import com.qg.dormrepair.config.JwtProperties;
import com.qg.dormrepair.exception.JwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;
import io.jsonwebtoken.security.Keys;

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
        //构建 JWT 签名
        SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSignKey().getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .addClaims(claims)
                .setExpiration(expiration)
                .signWith(secretKey)
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
            SecretKey secretKey = Keys.hmacShaKeyFor(jwtProperties.getSignKey().getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Token 解析失败", e);
            throw new JwtException("Token 解析失败");
        }
    }

    /**
     * 从Token中获取权限集合（兼容List/Set类型）
     * 解决：JWT解析后权限可能是 List 或 Set 类型，统一转成 Set<String> 方便使用
     */
    public Set<String> getPermissionsFromToken(Claims claims) {
        try {

            // 从JWT的载荷中取出 permissions 字段（存储的是用户权限列表）
            Object obj = claims.get("permissions");

            // ===================== 核心兼容逻辑 =====================
            // 作用：无论权限是 List 类型、Set 类型、数组类型，都能统一处理
            // Iterable<?> 是所有集合（List/Set）的父接口，instanceof 判断是不是集合
            if (obj instanceof Iterable<?>) {
                // 创建一个空的 Set<String>，用于存放最终的权限字符串
                Set<String> result = new HashSet<>();

                // 遍历集合中的每一个权限元素（强制转换成 Iterable 才能循环）
                for (Object item : (Iterable<?>) obj) {
                    // 把每个权限对象 转为 String 类型，存入Set
                    // 防止权限是数字、枚举等其他类型，统一转字符串最安全
                    result.add(item.toString());
                }

                // 返回统一格式的权限集合 Set<String>
                return result;
            }
            // 如果不是集合类型（比如null、字符串、数字等），直接返回null
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