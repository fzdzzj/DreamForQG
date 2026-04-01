package com.qg.dormrepair.pojo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
public class OperationLogEnity {
    private Long id;
    private String userAccount;//用户账号
    private String operation;//操作内容
    private String result;//操作结果（1-成功 0-失败）
    private String ipAddress;//客户端IP
    private LocalDateTime createTime;
    private String requestUri;
    private String requestParams;
}
