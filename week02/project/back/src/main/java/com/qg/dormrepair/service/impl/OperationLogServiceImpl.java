package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.OperationLogDao;
import com.qg.dormrepair.pojo.OperationLogEnity;
import com.qg.dormrepair.service.OperationLogService;
import com.qg.dormrepair.vo.PageResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 操作日志服务实现类
 * 提供操作日志的分页查询、批量删除功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogDao operationLogDao;

    /**
     * 分页条件查询操作日志
     *
     * @param userAccount 操作人账号（可选）
     * @param result      操作结果（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @param pageNum     页码
     * @param pageSize    每页条数
     * @return 分页结果
     */
    @Override
    public PageResult<OperationLogEnity> getLogs(String userAccount, String result,
                                                 LocalDateTime startTime, LocalDateTime endTime,
                                                 Integer pageNum, Integer pageSize) {
        log.info("开始分页查询操作日志，操作人：{}，操作结果：{}，开始时间：{}，结束时间：{}，页码：{}，页大小：{}",
                userAccount, result, startTime, endTime, pageNum, pageSize);

        // 校验分页参数合法性
        if (pageNum == null || pageNum < 1) {
            log.error("分页查询操作日志失败，页码不合法：{}", pageNum);
            throw new BusinessException("页码不能小于1");
        }
        if (pageSize == null || pageSize < 1) {
            log.error("分页查询操作日志失败，每页条数不合法：{}", pageSize);
            throw new BusinessException("每页条数不能小于1");
        }

        // 计算分页偏移量
        int offset = (pageNum - 1) * pageSize;

        // 条件查询日志列表
        List<OperationLogEnity> list = operationLogDao.selectByCondition(
                userAccount, result, startTime, endTime, offset, pageSize);

        // 查询总条数
        Long total = operationLogDao.countByCondition(userAccount, result);

        log.info("分页查询操作日志成功，总条数：{}，当前页条数：{}", total, list.size());

        // 封装分页结果返回
        return new PageResult<>(list, total, pageNum, pageSize);
    }

    /**
     * 批量删除操作日志
     *
     * @param logIds 日志ID数组
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLogs(Long[] logIds) {
        log.info("开始批量删除操作日志，日志ID数组：{}", Arrays.toString(logIds));

        // 参数校验
        if (logIds == null || logIds.length == 0) {
            log.warn("批量删除操作日志失败，日志ID不能为空");
            throw new BusinessException("请选择需要删除的日志");
        }

        // 执行批量删除
         Long rows =operationLogDao.deleteLogs(logIds);
            if (rows != logIds.length) {
                log.error("批量删除操作日志失败，实际删除数量：{}，期望删除数量：{}", rows, logIds.length);
                throw new BusinessException(400,"删除失败");
            }
        log.info("批量删除操作日志成功，删除数量：{}", logIds.length);
    }
}