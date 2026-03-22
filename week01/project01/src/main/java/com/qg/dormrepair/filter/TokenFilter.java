// filter/TokenFilter.java
package com.qg.dormrepair.filter;

import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.JwtUtils;
import com.qg.dormrepair.util.RegexUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@WebFilter(filterName = "TokenFilter", urlPatterns = "/api/*")
public class TokenFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("===== TokenFilter 初始化完成 =====");
    }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain)
            throws IOException, ServletException {

        long startTime = System.currentTimeMillis();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.info("===== [TokenFilter] 请求开始 =====");
        log.info("  时间戳：{} | 方法：{} | 路径：{}", startTime, method, requestURI);

        // ==================== 1. 公共接口放行（不需要 Token）====================
        if (isPublicEndpoint(requestURI)) {
            log.info("  类型：公共接口，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        // ==================== 2. 从 Authorization 头获取 token ====================
        String token = request.getHeader("Authorization");
        log.debug("  Authorization: {}", token);

        if (token == null || token.trim().isEmpty()) {
            log.warn("  ⚠️ 令牌为空，返回 401");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录，请先登录\",\"data\":null}");
            logFilterEnd(startTime, "令牌为空");
            return;
        }

        // ==================== 3. 去除 Bearer 前缀 ====================
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
            log.debug("  去除 Bearer 后的 Token: {}", token);
        }

        // ==================== 4. 验证 token ====================
        try {
            Claims claims = JwtUtils.parseJWT(token);
            String account = claims.get("account", String.class);
            String dormBuilding = claims.get("dormBuilding", String.class);
            String dormRoom = claims.get("dormRoom", String.class);
            CurrentHolder.setCurrentUser(account,dormBuilding,dormRoom);
            log.info("  ✅ ThreadLocal 设置成功，账号：", account);
            Character role;
            if(RegexUtil.isAdminId( account)){
                role = '2';
            }else{
                role = '1';
            }
            // ==================== 5. 角色权限验证 ====================
            if (!checkRolePermission(requestURI, method, role)) {
                log.warn("  ⚠️ 权限不足，角色：{}，路径：{}", role, requestURI);
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"message\":\"权限不足，无法访问此接口\",\"data\":null}");
                logFilterEnd(startTime, "权限不足");
                return;
            }
            log.info("  权限验证通过，角色：{}", role);

        } catch (ExpiredJwtException e) {
            log.warn("  ⚠️ Token 已过期");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"登录已过期，请重新登录\",\"data\":null}");
            logFilterEnd(startTime, "Token 过期");
            return;

        } catch (MalformedJwtException e) {
            log.warn("  ⚠️ Token 格式错误");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token 无效\",\"data\":null}");
            logFilterEnd(startTime, "Token 格式错误");
            return;

        } catch (Exception e) {
            log.error("  ❌ Token 验证异常", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"令牌不合法\",\"data\":null}");
            logFilterEnd(startTime, "Token 验证异常");
            return;
        }

        try {
            filterChain.doFilter(request, response);
            logFilterEnd(startTime, "成功");
        } finally {
            CurrentHolder.remove();
            log.debug("  🧹 ThreadLocal 已清理");
        }
    }

    /**
     * 判断是否为公共接口（不需要 Token）
     */
    private boolean isPublicEndpoint(String requestURI) {
        // 登录/注册接口
        if (requestURI.contains("/api/auth/login")) {
            return true;
        }
        if (requestURI.contains("/api/auth/register")) {
            return true;
        }
        return false;
    }

    /**
     * 角色权限验证
     * @param requestURI 请求路径
     * @param method 请求方法
     * @param role 用户角色（1-学生，2-管理员）
     * @return 是否有权限
     */
    private boolean checkRolePermission(String requestURI, String method, Character role) {
        // 角色为空，拒绝访问
        if (role == null) {
            return false;
        }

        // ==================== 学生角色（role = '1'）====================
        if (role == '1') {
            // ✅ 学生专属接口
            if (requestURI.startsWith("/api/student/")) {
                // 创建报修单
                if ("/api/student/order".equals(requestURI) && "POST".equals(method)) {
                    return true;
                }
                // 查看我的报修单列表
                if ("/api/student/orders".equals(requestURI) && "GET".equals(method)) {
                    return true;
                }
                // 查看报修单详情
                if (requestURI.matches("/api/student/order/\\d+") && "GET".equals(method)) {
                    return true;
                }
                // 取消报修单
                if (requestURI.matches("/api/student/order/\\d+/cancel") && "PUT".equals(method)) {
                    return true;
                }
                // 绑定宿舍
                if ("/api/student/dorm".equals(requestURI) && "PUT".equals(method)) {
                    return true;
                }
                // 修改密码
                if ("/api/student/password".equals(requestURI) && "PUT".equals(method)) {
                    return true;
                }
                return false;
            }

            //  消息接口（学生可访问）
            if (requestURI.startsWith("/api/message/")) {
                return true;
            }

            //  管理员接口（学生禁止）
            if (requestURI.startsWith("/api/admin/")) {
                return false;
            }

            // 其他接口默认拒绝
            return false;
        }

        // ==================== 管理员角色（role = '2'）====================
        if (role == '2') {
            //  管理员专属接口
            if (requestURI.startsWith("/api/admin/")) {
                // 查看所有报修单
                if ("/api/admin/orders".equals(requestURI) && "GET".equals(method)) {
                    return true;
                }
                // 查看报修单详情
                if (requestURI.matches("/api/admin/order/\\d+") && "GET".equals(method)) {
                    return true;
                }
                // 更新报修单状态
                if (requestURI.matches("/api/admin/order/\\d+/status") && "PUT".equals(method)) {
                    return true;
                }
                // 删除报修单
                if (requestURI.matches("/api/admin/order/\\d+") && "DELETE".equals(method)) {
                    return true;
                }
                // 多条件查询
                if ("/api/admin/orders/query".equals(requestURI) && "POST".equals(method)) {
                    return true;
                }
                // 按状态查询
                if ("/api/admin/orders/by-status".equals(requestURI) && "GET".equals(method)) {
                    return true;
                }
                // 按楼栋查询
                if ("/api/admin/orders/by-dorm".equals(requestURI) && "GET".equals(method)) {
                    return true;
                }
                return true; // 其他/admin/接口也允许
            }

            // ✅ 消息接口（管理员可访问）
            if (requestURI.startsWith("/api/message/")) {
                return true;
            }

            // ✅ 修改密码（公共）
            if ("/api/student/password".equals(requestURI) && "PUT".equals(method)) {
                return true;
            }

            //  学生专属接口（管理员禁止）
            if (requestURI.startsWith("/api/student/")) {
                return false;
            }

            // 其他接口默认允许
            return true;
        }

        // 未知角色，拒绝访问
        return false;
    }

    /**
     * 记录 Filter 执行结束日志
     */
    private void logFilterEnd(long startTime, String status) {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("===== [TokenFilter] 请求结束 =====");
        log.info("  结束时间戳：{} | 耗时：{}ms | 状态：{}", endTime, duration, status);
    }

    @Override
    public void destroy() {
        log.info("===== TokenFilter 销毁 =====");
    }
}
