package com.qg.dormrepair.aspect;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.mapper.OperationLogDao;
import com.qg.dormrepair.pojo.OperationLogEnity;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.JwtUtils; // 替换成你项目的实际JWT工具类路径
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

/**
 * 操作日志切面（适配登录/注册/退出登录全场景）
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogDao operationLogDao;
    private final JwtUtils jwtUtils;

    /** 请求参数最大长度 */
    private static final int PARAMS_MAX_LENGTH = 2000;
    /** 操作成功状态码 */
    private static final String RESULT_SUCCESS = "1";
    /** 操作失败状态码 */
    private static final String RESULT_FAIL = "2";
    /** 匿名用户标识 */
    private static final String ANONYMOUS_USER = "anonymous";

    /**
     * 环绕通知：拦截带有 @OperationLog 注解的方法
     */
    @Around(value = "@annotation(logAnnotation)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog logAnnotation) throws Throwable {
        // 安全获取request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = (attributes != null) ? attributes.getRequest() : null;

        // ========== 核心优化：提前提取账号 ==========
        String userAccount = getAccountForAllScenarios(joinPoint, logAnnotation);

        // 构建日志实体
        OperationLogEnity logEntity = buildLogEntity(joinPoint, logAnnotation, request, userAccount);
        logEntity.setCreateTime(LocalDateTime.now());

        try {
            // 执行业务方法
            Object result = joinPoint.proceed();
            // 标记成功
            logEntity.setResult(RESULT_SUCCESS);
            saveLog(logEntity);
            log.info("[操作日志] {} | 用户：{} | 参数：{}",
                    logEntity.getOperation(), logEntity.getUserAccount(), logEntity.getRequestParams());
            return result;
        } catch (Throwable t) {
            // 标记失败
            logEntity.setResult(RESULT_FAIL);
            saveLog(logEntity);
            log.error("[操作失败] {} | 用户：{} | 异常：",
                    logEntity.getOperation(), logEntity.getUserAccount(), t);
            throw t;
        }
    }

    /**
     * 适配所有场景的账号提取逻辑（登录/注册/退出登录/普通接口）
     */
    private String getAccountForAllScenarios(ProceedingJoinPoint joinPoint, OperationLog logAnnotation) {
        String operation = logAnnotation.value();
        String account = null;

        // ========== 退出登录场景 - 从Token解析账号 ==========
        if ("退出登录".equals(operation)) {
            // 和接口@OperationLog("退出登录")保持一致
            // 从接口参数中提取accessToken
            String accessToken = getAccessTokenFromLogoutParams(joinPoint.getArgs());
            if (accessToken != null && !accessToken.isBlank()) {
                // 解析Token获取账号
                try {
                    Map<String, Object> claims = jwtUtils.parseToken(accessToken);
                    // 校验claims和account非空
                    if (claims != null && claims.get("account") != null) {
                        account = (String) claims.get("account"); // Token中存账号的key
                    }
                } catch (Exception e) {
                    log.warn("退出登录-解析Token获取账号失败：{}", e.getMessage());
                }
            }

            // 如果Token解析失败，再尝试从上下文拿（兜底）
            if (account == null) {
                var currentUser = CurrentHolder.getCurrentUser();
                account = currentUser != null ? currentUser.getAccount() : null;
            }
        }

        // 场景2：登录/注册（从DTO参数提取account字段）
        if (account == null && ("用户登录".equals(operation) || "用户注册".equals(operation))) {
            account = getAccountFromDTO(joinPoint.getArgs());
        }

        // 场景3：普通接口（从上下文拿）
        if (account == null) {
            var currentUser = CurrentHolder.getCurrentUser();
            account = currentUser == null ? ANONYMOUS_USER : currentUser.getAccount();
        }

        return account == null ? ANONYMOUS_USER : account;
    }

    /**
     * 从退出登录接口的参数中提取accessToken
     */
    private String getAccessTokenFromLogoutParams(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        // 遍历参数，找到Authorization头（String类型，格式：Bearer xxx）
        String accessToken = null;
        for (Object arg : args) {
            if (arg instanceof String && ((String) arg).startsWith("Bearer ")) {
                accessToken = ((String) arg).substring(7);
                break;
            }
            // 兜底：遍历所有String参数
            if (arg instanceof String) {
                String strArg = (String) arg;
                if (strArg.startsWith("Bearer ")) {
                    accessToken = strArg.substring(7);
                    break;
                }
            }
        }
        return accessToken;
    }

    /**
     * 从登录/注册DTO中精准提取account字段（适配你的DTO结构）
     */
    private String getAccountFromDTO(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }

        for (Object arg : args) {
            if (arg == null) {
                continue;
            }

            try {
                // 反射account字段
                Field accountField = arg.getClass().getDeclaredField("account");
                accountField.setAccessible(true);
                Object accountValue = accountField.get(arg);
                if (accountValue != null && accountValue instanceof String && !((String) accountValue).isBlank()) {
                    return (String) accountValue;
                }
            } catch (NoSuchFieldException e) {
                log.warn("参数对象无account字段：{}", arg.getClass().getName());
            } catch (IllegalAccessException e) {
                log.warn("反射获取account失败：{}", e.getMessage());
            }
        }
        return null;
    }

    /**
     * 构建日志实体（直接传入提取好的账号）
     */
    private OperationLogEnity buildLogEntity(ProceedingJoinPoint joinPoint, OperationLog logAnnotation,
                                             HttpServletRequest request, String userAccount) {
        OperationLogEnity logEntity = new OperationLogEnity();
        logEntity.setOperation(logAnnotation.value());
        logEntity.setUserAccount(userAccount); // 直接用提前提取的账号

        // 设置IP和URI
        if (request != null) {
            logEntity.setIpAddress(getClientIp(request));
            logEntity.setRequestUri(request.getRequestURI());
        }

        // 设置请求参数（超长截断）
        String params = Arrays.toString(joinPoint.getArgs());
        logEntity.setRequestParams(params.length() > PARAMS_MAX_LENGTH
                ? params.substring(0, PARAMS_MAX_LENGTH)
                : params);

        return logEntity;
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            ip = ip.contains(",") ? ip.split(",")[0].trim() : ip;
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 异步保存日志（异常不影响业务）
     */
    @Async
    public void saveLog(OperationLogEnity logEntity) {
        try {
            operationLogDao.insert(logEntity);
        } catch (Exception e) {
            log.error("日志保存失败：{}", e.getMessage(), e);
        }
    }
}