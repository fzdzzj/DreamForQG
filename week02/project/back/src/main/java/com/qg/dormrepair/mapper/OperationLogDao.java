package com.qg.dormrepair.mapper;

import com.qg.dormrepair.annotation.AutoFill;
import com.qg.dormrepair.constants.AutoFillConstant;
import com.qg.dormrepair.enums.DataBaseOperationType;
import com.qg.dormrepair.pojo.OperationLogEnity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OperationLogDao {

    // 插入日志
    @Insert("INSERT INTO operation_log (user_account, operation, result, ip_address, create_time, request_uri, request_params) " +
            "VALUES (#{userAccount}, #{operation}, #{result}, #{ipAddress}, #{createTime}, #{requestUri}, #{requestParams})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationLogEnity log);

    // 多条件查询
    List<OperationLogEnity> selectByCondition(@Param("userAccount") String userAccount,
                                              @Param("result") String result,
                                              @Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("offset") int offset,
                                              @Param("pageSize") int pageSize);

    // 统计总数
    Long countByCondition(@Param("userAccount") String userAccount,
                          @Param("result") String result);

    // 批量删除日志
    Long deleteLogs(Long[] logIds);
}