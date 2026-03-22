package com.qg.dormrepair.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 当前线程用户信息工具类
 */
public class CurrentHolder {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserContext{
        private String account;
        private String dormBuilding;
        private String dormRoom;
    }
    /**
     * 使用 ThreadLocal 存储当前线程的用户信息
     */
    private static final ThreadLocal<UserContext> CURRENT_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前线程的用户的信息
     *
     * @param account
     * @param dormBuilding
     * @param dormRoom
     */
    public static void setCurrentUser(String account,String dormBuilding, String dormRoom) {
        CURRENT_LOCAL.set(new UserContext(account, dormBuilding, dormRoom));
    }


    /**
     * 获取当前线程的完整用户上下文
     *
     * @return 用户上下文对象，若未设置则返回 null
     */
    public static UserContext getCurrentUser() {
        return CURRENT_LOCAL.get();
    }
    /**
     * 清除当前线程的学生（防止内存泄漏）
     * 通常在请求结束时调用（如 Filter 的 finally 块中）
     */
    public static void remove() {
        CURRENT_LOCAL.remove();
    }
}
