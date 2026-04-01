package com.qg.dormrepair.filter;

import com.qg.dormrepair.enums.Role;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.service.impl.TokenRefreshServiceImpl;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Set;

/**
 * 过滤器
 */
@Slf4j
@WebFilter(filterName = "TokenFilter", urlPatterns = "/api/*")
@RequiredArgsConstructor
public class TokenFilter implements Filter {
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final TokenRefreshServiceImpl tokenRefreshService;

    // 公共接口
    private static final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh",
            "/api/auth/logout"
    };

    @Override
    public void init(FilterConfig filterConfig) {
        log.info("===== TokenFilter 初始化 =====");
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {
            // 1. 公共接口直接放行
            if (isPublicUrl(uri)) {
                log.info("公共接口放行：{} {}", method, uri);
                filterChain.doFilter(request, response);
                return;
            }

            // 2. 获取 Token
            String token = getTokenFromRequest(request);
            if (token == null) {
                log.warn("未登录：{} {}", method, uri);
                writeJson(response, 401, "未登录，请先登录");
                return;
            }

            // 3. 黑名单校验
            if (tokenRefreshService.isBlacklisted(token)) {
                log.warn("Token在黑名单中：{} {}", method, uri);
                writeJson(response, 401, "Token已失效");
                return;
            }

            // 4. 解析 Token
            Claims claims = jwtUtils.parseToken(token);
            if (claims == null) {
                log.warn("Token解析失败：{} {}", method, uri);
                writeJson(response, 401, "令牌不合法");
                return;
            }

            // 5. 必须使用 accessToken
            String tokenType = claims.get("type", String.class);
            if (!"access".equals(tokenType)) {
                log.warn("禁止使用RefreshToken访问接口：{} {}", method, uri);
                writeJson(response, 401, "Token类型错误");
                return;
            }

            // 6. 获取用户信息
            String account = claims.get("account", String.class);
            String role = claims.get("role", String.class);
            CurrentHolder.setCurrentUser(account, role);


            // ===================== 数据库 RBAC 核心 =====================
            // 从 TOKEN 中取出 权限列表（数据库查询后存入JWT）
            Set<String> permissions = jwtUtils.getPermissionsFromToken(claims);
            // 7. 权限校验
            if (!hasPermission(uri, method, permissions)) {
                log.warn("权限不足：{} {}", method, uri);
                writeJson(response, 403, "权限不足");
                return;
            }

            // 8. 学生必须绑定宿舍
            if (Role.STUDENT.getCode().equals(role)) {
                if (uri.startsWith("/api/student/") && !uri.startsWith("/api/student/dorm")) {
                    if (!userService.isDormBound(account)) {
                        log.warn("学生未绑定宿舍：{} {}", method, uri);
                        writeJson(response, 403, "请先绑定宿舍");
                        return;
                    }
                }
            }

            // 9. 全部校验通过，放行
            filterChain.doFilter(request, response);

        } finally {
            CurrentHolder.remove();
        }
    }

    // ==================== 工具方法 ====================
    private String getTokenFromRequest(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return null;
        return auth.substring(7).trim();
    }

    private boolean isPublicUrl(String uri) {
        for (String url : PUBLIC_URLS) {
            if (uri.startsWith(url)) return true;
        }
        return false;
    }

    private void writeJson(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(code == 401 ? HttpServletResponse.SC_UNAUTHORIZED : HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(String.format("{\"code\":%d,\"message\":\"%s\",\"data\":null}", code, msg));
    }

    /**
     * URI 标准化：/api/student/order/123 → /api/student/order/{id}
     */
    private String normalizeUri(String uri) {
        if (uri == null) return "";
        return uri.replaceAll("/\\d+", "/{id}");
    }

    // ==================== 数据库 RBAC 权限判断 ====================
    private boolean hasPermission(String uri, String method, Set<String> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        String normalizedUri = normalizeUri(uri);
        String currentPerm = (method + ":" + normalizedUri).trim(); // 这里也 trim

        return permissions.stream()
                .map(String::trim) // 这里也 trim
                .anyMatch(currentPerm::equals);
    }

    @Override
    public void destroy() {
        log.info("===== TokenFilter 销毁 =====");
    }
}