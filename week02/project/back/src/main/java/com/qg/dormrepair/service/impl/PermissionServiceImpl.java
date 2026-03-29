package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.PermissionDao;
import com.qg.dormrepair.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务实现类
 * 提供根据角色编码查询权限集合的功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionDao permissionDao;

    /**
     * 根据角色编码获取对应的权限标识集合
     *
     * @param roleCode 角色编码
     * @return 权限标识Set集合（自动去重）
     */
    @Override
    public Set<String> getPermissionsByRole(String roleCode) {
        log.info("开始根据角色编码查询权限，角色编码：{}", roleCode);

        // 参数非空校验
        if (!StringUtils.hasText(roleCode)) {
            log.error("根据角色查询权限失败，角色编码不能为空");
            throw new BusinessException("角色编码不能为空");
        }

        // 查询数据库获取权限列表
        List<String> permissionList = permissionDao.selectPermissionsByRoleCode(roleCode);
        log.info("根据角色【{}】查询到权限数量：{}", roleCode, permissionList.size());

        // 转换为Set集合去重并返回
        Set<String> permissionSet = new HashSet<>(permissionList);
        log.info("角色【{}】最终有效权限数量：{}", roleCode, permissionSet.size());

        return permissionSet;
    }
}