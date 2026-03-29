package com.qg.dormrepair.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前线程用户信息上下文工具类
 * 基于 ThreadLocal 实现，在整个请求周期内存储当前登录用户的账号与角色信息
 * 用于在 Controller / Service / Mapper 层随时获取当前用户身份
 */
public class CurrentHolder {

    /**
     * 用户上下文内部类
     * 存储当前登录用户的核心身份信息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserContext {
        /**
         * 用户账号（学号/管理员账号）
         */
        private String account;

        /**
         * 用户角色（student/admin）
         */
        String role;
    }

    /**
     * 线程隔离存储容器：每个请求独立存储自己的用户信息
     */
    private static final ThreadLocal<UserContext> CURRENT_LOCAL = new ThreadLocal<>();

    /**
     * 往当前线程存入用户身份信息
     *
     * @param account 用户账号
     * @param role    用户角色
     */
    public static void setCurrentUser(String account, String role) {
        CURRENT_LOCAL.set(new UserContext(account, role));
    }

    /**
     * 获取当前线程中的用户上下文信息
     *
     * @return UserContext 用户信息对象
     */
    public static UserContext getCurrentUser() {
        return CURRENT_LOCAL.get();
    }

    /**
     * 清除当前线程的用户信息
     * 必须在请求结束时调用，避免内存泄漏
     */
    public static void remove() {
        CURRENT_LOCAL.remove();
    }
}