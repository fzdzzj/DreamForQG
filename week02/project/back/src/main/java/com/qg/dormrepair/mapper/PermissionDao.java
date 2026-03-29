package com.qg.dormrepair.mapper;

import java.util.List;
/**
 * 权限数据访问接口
 */
public interface PermissionDao {
    List<String> selectPermissionsByRoleCode(String roleCode);
}