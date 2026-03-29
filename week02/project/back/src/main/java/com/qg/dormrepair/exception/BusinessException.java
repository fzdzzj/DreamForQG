package com.qg.dormrepair.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * 用于业务逻辑校验失败，如：参数错误、状态异常、权限不足等
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 通用默认错误码
     */
    public static final int DEFAULT_CODE = 500;

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 只有错误信息（默认500）
     */
    public BusinessException(String message) {
        super(message);
        this.code = DEFAULT_CODE;
    }

    /**
     * 错误码 + 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 错误信息 + 原始异常（默认500）
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = DEFAULT_CODE;
    }

    /**
     * 错误码 + 错误信息 + 原始异常
     */
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

}