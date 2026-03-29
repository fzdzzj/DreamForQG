package com.qg.dormrepair.util;

import com.qg.dormrepair.constants.RegexConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * 正则校验工具类
 * 提供密码、学号、管理员ID、用户ID等格式校验功能
 */
@Slf4j
public class RegexUtil {

    /**
     * 私有构造方法，防止工具类被实例化
     */
    private RegexUtil() {
    }

    /**
     * 校验密码格式是否合法
     *
     * @param password 待校验密码
     * @return true-合法，false-不合法
     */
    public static boolean isPassword(String password) {
        log.debug("验证密码格式");
        return password.matches(RegexConstants.PASSWORD);
    }

    /**
     * 校验学号格式是否合法
     *
     * @param studentId 待校验学号
     * @return true-合法，false-不合法
     */
    public static boolean isStudentId(String studentId) {
        log.debug("验证学号格式");
        return studentId.matches(RegexConstants.STUDENT_ID);
    }

    /**
     * 校验管理员ID格式是否合法
     *
     * @param adminId 待校验管理员ID
     * @return true-合法，false-不合法
     */
    public static boolean isAdminId(String adminId) {
        log.debug("验证管理员ID格式");
        return adminId.matches(RegexConstants.ADMIN_ID);
    }

    /**
     * 校验用户ID（通用）格式是否合法
     *
     * @param userId 待校验用户ID
     * @return true-合法，false-不合法
     */
    public static boolean isUserId(String userId) {
        return userId.matches(RegexConstants.USER_ID);
    }
}