package com.qg.dormrepair.exception;

import com.qg.dormrepair.pojo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;
/**
 * 全局异常处理器
 * <p>
 * 统一捕获并处理系统中所有控制器层抛出的异常，返回标准化的Result响应结果，
 * 避免前端接收到非预期的异常信息，同时通过日志记录异常详情便于排查
 * </p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理@Validated/@Valid参数校验异常（请求体参数校验失败）
     * <p>
     * 捕获Controller层接收@RequestBody参数时，校验注解（如@NotBlank、@Pattern）触发的异常，
     * 拼接所有字段的校验失败信息返回给前端
     * </p>
     * @param e MethodArgumentNotValidException 校验异常对象，包含字段错误信息
     * @return 统一响应结果，状态码400，message为所有字段的校验错误（格式：字段名:错误提示,字段名:错误提示）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e){
        String message=e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField()+":"+fieldError.getDefaultMessage())
                .collect(Collectors.joining(","));
        log.warn("参数校验失败：{}",message);
        return Result.error(400,message);
    }

    /**
     * 处理参数绑定异常（路径参数/请求参数绑定失败）
     * <p>
     * 捕获Controller层接收@PathVariable/@RequestParam参数时的类型转换、参数缺失等绑定异常，
     * 例如：将字符串转换为Character时传入多个字符、必填参数未传等
     * </p>
     * @param e BindException 参数绑定异常对象，包含字段错误信息
     * @return 统一响应结果，状态码400，消息为参数绑定错误详情
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e){
        String message=e.getFieldErrors().stream()
                .map(fieldError->fieldError.getField()+":"+fieldError.getDefaultMessage())
                .collect(Collectors.joining(","));
        log.warn("参数绑定失败：{}",message);
        return Result.error(400,message);
    }
    /**
     * 处理自定义业务异常
     * <p>
     * 捕获系统中手动抛出的BusinessException（如账号不存在、密码错误、订单状态异常等），
     * 直接返回异常中定义的错误码和错误信息，便于前端根据业务码做个性化提示
     * </p>
     * @param e BusinessException 自定义业务异常对象，包含错误码和错误消息
     * @return 统一响应结果，状态码为业务异常码，消息为业务异常描述
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e){
        log.warn("业务异常：{}",e.getMessage());
        return Result.error(e.getCode(),e.getMessage());
    }
    /**
     * 处理所有未捕获的通用异常（兜底异常处理）
     * <p>
     * 捕获上述异常之外的所有系统异常（如空指针、数据库异常、IO异常等），
     * 为避免暴露系统内部细节，返回通用提示信息，同时记录ERROR级别日志便于排查
     * </p>
     * @param e Exception 通用异常对象
     * @return 统一响应结果，默认错误码，消息为通用友好提示
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("服务器异常：{}",e.getMessage());
        return Result.error("系统繁忙，请稍后重试");
    }
    /**
     * 处理JWT异常
     * <p>
     * 捕获JWT异常（如Token解析失败、Token过期等），
     * 返回对应的错误码和错误信息
     * </p>
     * @param e JwtException JWT异常对象，包含错误码和错误消息
     * @return 统一响应结果，状态码为JWT异常码，消息为JWT异常描述
     */
    @ExceptionHandler(JwtException.class)
    public Result<Void> handleJwtException(JwtException e){
        log.warn("JWT异常：{}",e.getMessage());
        return Result.error(e.getCode(),e.getMessage());
    }
}
