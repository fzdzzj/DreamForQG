package com.qg.dormrepair.service;

import java.util.Set;

/**
 * 权限服务接口
 * 提供根据角色编码查询对应权限的核心功能
 */
public interface PermissionService {

    /**
     * 根据角色编码获取该角色拥有的权限标识集合
     *
     * @param roleCode 角色编码（如：admin、student）
     * @return 权限标识Set（自动去重）
     */
    Set<String> getPermissionsByRole(String roleCode);
}