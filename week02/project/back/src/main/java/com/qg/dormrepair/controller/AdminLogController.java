package com.qg.dormrepair.controller;

import com.qg.dormrepair.pojo.OperationLogEnity;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.service.OperationLogService;
import com.qg.dormrepair.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 管理员日志功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "管理员日志接口", description = "管理员的查看删除日志接口")
public class AdminLogController {
    private final OperationLogService operationLogService;

    /**
     * 查询操作日志（管理员专用）
     */
    @GetMapping
    @Operation(summary = "多条件查询操作日志", description = "支持用户账号、操作结果、时间范围、分页查询")
    public Result<PageResult<OperationLogEnity>> getLogs(
            @Parameter(description = "用户账号", example = "3125004123") @RequestParam(required = false) String userAccount,
            @Parameter(description = "操作结果 1=成功 2=失败", example = "1") @RequestParam(required = false) String result,

            @Parameter(description = "开始时间", example = "2025-01-01 00:00:00")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime startTime,

            @Parameter(description = "结束时间", example = "2025-12-31 23:59:59")
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime endTime,

            @Parameter(description = "当前页码", example = "1")
            @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        log.info("多条件查询操作日志");
        log.info("参数：userAccount={},result={},startTime={},endTime={},pageNum={},pageSize={}",
                userAccount, result, startTime, endTime, pageNum, pageSize);
        PageResult<OperationLogEnity> pageResult = operationLogService.getLogs(userAccount, result, startTime, endTime, pageNum, pageSize);
        log.info("多条件查询日志返回结果：{}", pageResult);
        return Result.success(pageResult);
    }

    /**
     * 批量删除操作日志
     */
    @DeleteMapping
    @Operation(summary = "批量删除操作日志", description = "根据日志ID数组批量删除记录")
    public Result<Void> deleteLogs(@Parameter(description = "日志ID数组", example = "[1,2,3]") @RequestParam Long[] logIds) {
        log.info("批量删除操作日志，日志ID数组：{}", Arrays.toString(logIds));
        operationLogService.deleteLogs(logIds);
        log.info("批量删除操作日志成功");
        return Result.success();
    }
}