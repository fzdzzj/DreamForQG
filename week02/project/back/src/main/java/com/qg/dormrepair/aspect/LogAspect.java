package com.qg.dormrepair.aspect;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.OperationLogDao;
import com.qg.dormrepair.pojo.OperationLogEnity;
import com.qg.dormrepair.util.CurrentHolder;
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

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面
 * 统一记录系统操作日志，包括操作人、IP、请求参数、执行结果
 * 异步保存，不影响业务接口性能
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final OperationLogDao operationLogDao;

    /** 请求参数最大长度 */
    private static final int PARAMS_MAX_LENGTH = 2000;

    /** 操作成功状态码 */
    private static final String RESULT_SUCCESS = "1";

    /** 操作失败状态码 */
    private static final String RESULT_FAIL = "2";

    /** 匿名用户标识 */
    private static final String ANONYMOUS_USER = "anonymous";

    /**
     * 环绕通知：拦截带有 @OperationLog 注解的方法，记录操作日志
     */
    @Around(value = "@annotation(logAnnotation)")
    public Object logOperation(ProceedingJoinPoint joinPoint, OperationLog logAnnotation) throws Throwable {
        // 安全获取request对象，避免非Web环境下空指针
        //浏览器请求 → 进入 SpringBoot → Spring 会把这次请求的所有信息装进一个对象里
        //这个对象就是 RequestContextHolder
        //调用 getRequestAttributes() 就能拿到当前请求的全部信息
        //强转成 ServletRequestAttributes，是因为它专门装 HTTP 请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //安全地拿出 request 对象，如果没有请求（非 Web 环境）就返回 null，不报错
        HttpServletRequest request = (attributes != null) ? attributes.getRequest() : null;

        // 构建日志实体
        OperationLogEnity logEntity = buildLogEntity(joinPoint, logAnnotation, request);

        try {
            // 执行业务方法
            Object result = joinPoint.proceed();

            // 执行成功，标记成功状态并异步保存日志
            logEntity.setResult(RESULT_SUCCESS);
            saveLog(logEntity);
            log.info("[操作日志] {} | 用户：{} | 参数：{}",
                    logEntity.getOperation(), logEntity.getUserAccount(), logEntity.getRequestParams());
            return result;
        } catch (Throwable t) {
            // 执行失败，标记失败状态并异步保存日志
            logEntity.setResult(RESULT_FAIL);
            saveLog(logEntity);
            log.error("[操作失败] {} | 用户：{} | 异常：",
                    logEntity.getOperation(), logEntity.getUserAccount(), t);

            // 抛出业务异常，保持原有异常流程
            throw t;
        }
    }

    /**
     * 构建操作日志实体
     */
    private OperationLogEnity buildLogEntity(ProceedingJoinPoint joinPoint, OperationLog logAnnotation, HttpServletRequest request) {
        OperationLogEnity logEntity = new OperationLogEnity();
        logEntity.setOperation(logAnnotation.value());

        // 设置当前操作人
        var currentUser = CurrentHolder.getCurrentUser();
        logEntity.setUserAccount(currentUser == null ? ANONYMOUS_USER : currentUser.getAccount());

        // 设置请求IP与URI
        if (request != null) {
            logEntity.setIpAddress(getClientIp(request));
            logEntity.setRequestUri(request.getRequestURI());
        }

        // 设置请求参数，超长自动截断
        String params = Arrays.toString(joinPoint.getArgs());
        logEntity.setRequestParams(params.length() > PARAMS_MAX_LENGTH
                ? params.substring(0, PARAMS_MAX_LENGTH)
                : params);

        return logEntity;
    }

    /**
     * 获取客户端真实IP地址
     * <p>解决Nginx/网关等反向代理后获取到服务器IP的问题</p>
     * <p>优先级：X-Forwarded-For > X-Real-IP > remoteAddr</p>
     * @param request HTTP请求对象
     * @return 客户端真实IP，不可能为null
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0].trim();
            }
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
     * 异步保存操作日志
     * 保存异常只打印日志，不影响业务流程
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