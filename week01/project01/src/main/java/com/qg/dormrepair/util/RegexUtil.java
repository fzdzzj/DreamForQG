package com.qg.dormrepair.util;

import com.qg.dormrepair.constants.RegexConstants;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegexUtil {
    private RegexUtil() {
    }
    public static boolean isPassword(String password) {
        log.debug("验证密码格式");
        return password.matches(RegexConstants.PASSWORD);
    }
    public static boolean isStudentId(String studentId) {
        log.debug("验证学号格式");
        return studentId.matches(RegexConstants.STUDENT_ID);
    }
    public static boolean isAdminId(String adminId) {
        log.debug("验证管理员ID格式");
        return adminId.matches(RegexConstants.ADMIN_ID);
    }
    public static boolean isUserId(String userId) {
        return userId.matches(RegexConstants.USER_ID);
    }
}
