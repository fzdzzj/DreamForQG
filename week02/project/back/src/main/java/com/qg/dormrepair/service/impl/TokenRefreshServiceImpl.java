package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.constants.MessageConstant;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.service.PermissionService;
import com.qg.dormrepair.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Token 刷新服务实现类
 * 负责刷新 AccessToken、刷新令牌黑名单管理、Token 合法性校验
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshServiceImpl {

    /**
     * JWT 工具类
     */
    private final JwtUtils jwtUtils;

    /**
     * 权限服务：用于查询用户最新权限
     */
    private final PermissionService permissionService;

    /**
     * RefreshToken 黑名单
     * key：token 值
     * value：加入黑名单时间戳
     */
    private final Map<String, Long> tokenBlacklist = new HashMap<>();

    /**
     * 刷新 AccessToken（使用 RefreshToken 获取新的 AccessToken）
     * 会从数据库查询最新权限，重新生成 AccessToken
     *
     * @param refreshToken 刷新令牌
     * @return 包含新 accessToken 的集合
     */
    public Map<String, String> refreshTokens(String refreshToken) {
        log.info("开始执行 AccessToken 刷新操作");

        // 参数校验
        if (refreshToken == null || refreshToken.isBlank()) {
            log.error("刷新 Token 失败："+MessageConstant.REFRESH_TOKEN_NOT_EMPTY);
            throw new BusinessException(401, MessageConstant.REFRESH_TOKEN_NOT_EMPTY);
        }

        // 1. 黑名单校验：已拉黑的 Token 直接拒绝
        if (tokenBlacklist.containsKey(refreshToken)) {
            log.warn("刷新 Token 失败：RefreshToken 已被加入黑名单，Token：{}", refreshToken);
            throw new BusinessException(401,MessageConstant.REFRESH_TOKEN_INVALID+"，请重新登录");
        }
        log.info("RefreshToken 黑名单校验通过");

        // 2. 过期校验
        if (jwtUtils.isTokenExpired(refreshToken)) {
            log.warn("刷新 Token 失败:"+MessageConstant.REFRESH_TOKEN_EXPIRED+"，Token：{}", refreshToken);
            throw new BusinessException(401,MessageConstant.REFRESH_TOKEN_EXPIRED+"，请重新登录");
        }
        log.info("RefreshToken 过期校验通过");

        // 3. Token 类型必须是 refresh
        String type = jwtUtils.getTypeFromToken(refreshToken);
        if (!"refresh".equals(type)) {
            log.error("刷新 Token 失败："+MessageConstant.TOKEN_TYPE_ILLEGAL+"，当前类型：{}", type);
            throw new BusinessException(401,MessageConstant.TOKEN_TYPE_ILLEGAL);
        }
        log.info("RefreshToken 类型校验通过，类型：{}", type);

        // 4. 解析获取用户身份信息
        String account = jwtUtils.getAccountFromToken(refreshToken);
        String role = jwtUtils.getRoleFromToken(refreshToken);
        if (account == null || role == null) {
            log.error("刷新 Token 失败："+MessageConstant.TOKEN_INVALID+"，account={}，role={}", account, role);
            throw new BusinessException(401,MessageConstant.TOKEN_INVALID);
        }
        log.info("成功解析用户信息，账号：{}，角色：{}", account, role);

        // 5. 从数据库查询最新权限，保证权限实时生效
        log.info("开始从数据库查询用户【{}】最新权限", account);
        Set<String> permissions = permissionService.getPermissionsByRole(role);
        log.info("用户【{}】权限查询完成，权限数量：{}", account, permissions.size());

        // 6. 生成新的 AccessToken（携带最新权限）
        String newAccessToken = jwtUtils.generateAccessToken(account, role, permissions);
        log.info("新 AccessToken 生成成功，用户：{}", account);

        // 封装返回
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        log.info("AccessToken 刷新流程全部完成，用户：{}", account);

        return tokens;
    }

    /**
     * 将 Token 加入黑名单（用于登出/强制下线）
     *
     * @param token 需要拉黑的 Token
     */
    public void addTokenToBlacklist(String token) {
        // 空值直接跳过
        if (token == null || token.isEmpty()) {
            log.debug("加入黑名单失败：Token 为空");
            return;
        }

        // 加入黑名单，记录当前时间
        tokenBlacklist.put(token, System.currentTimeMillis());
        log.info("Token 已加入黑名单，当前黑名单大小：{}", tokenBlacklist.size());

        // 定期清理：超过 10000 条时，自动清理超过 1 天的过期 Token
        if (tokenBlacklist.size() > 10000) {
            log.info("黑名单容量超限，开始自动清理 1 天前的过期 Token");
            long now = System.currentTimeMillis();
            // 移除超过 24 小时的记录
            tokenBlacklist.entrySet().removeIf(entry -> now - entry.getValue() > 86400000);
            log.info("黑名单清理完成，清理后大小：{}", tokenBlacklist.size());
        }
    }

    /**
     * 判断 Token 是否在黑名单中
     *
     * @param token 待校验 Token
     * @return true=已拉黑，false=未拉黑
     */
    public boolean isBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        return tokenBlacklist.containsKey(token);
    }
}