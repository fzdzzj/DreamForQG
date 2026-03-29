package com.qg.dormrepair.service;

import com.qg.dormrepair.pojo.OperationLogEnity;
import com.qg.dormrepair.vo.PageResult;

import java.time.LocalDateTime;

/**
 * 操作日志服务接口
 * 提供系统操作日志的分页查询、批量删除功能
 */
public interface OperationLogService {

    /**
     * 多条件分页查询操作日志
     *
     * @param userAccount 操作人账号（可为null）
     * @param result      操作结果（成功/失败，可为null）
     * @param startTime   开始时间（可为null）
     * @param endTime     结束时间（可为null）
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @return 分页封装的操作日志列表
     */
    PageResult<OperationLogEnity> getLogs(String userAccount, String result,
                                          LocalDateTime startTime, LocalDateTime endTime,
                                          Integer pageNum, Integer pageSize);

    /**
     * 批量删除操作日志
     *
     * @param logIds 要删除的日志ID数组
     */
    void deleteLogs(Long[] logIds);
}