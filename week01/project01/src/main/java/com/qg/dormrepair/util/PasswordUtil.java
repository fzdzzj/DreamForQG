// util/PasswordUtil.java
package com.qg.dormrepair.util;

import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;

@Slf4j
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * MD5 加密（简单，但不推荐用于生产）
     */
    public static String encrypt(String password) {
        log.debug("密码加密操作开始");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] passwordBytes = password.getBytes("UTF-8");
            byte[] encryptedBytes = md.digest(passwordBytes);

            StringBuilder result = new StringBuilder();
            for (byte b : encryptedBytes) {
                // 将字节转换为十六进制字符串
                result.append(String.format("%02x", b));
            }

            log.debug("密码加密完成");
            return result.toString();
        } catch (Exception e) {
            log.error("密码加密异常", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * 密码匹配验证
     */
    public static boolean matches(String rawPassword, String encryptedPassword) {
        log.debug("密码验证操作开始");
        try {
            String encrypted = encrypt(rawPassword);
            boolean matches = encrypted.equals(encryptedPassword);
            log.debug("密码验证结果：{}", matches);
            return matches;
        } catch (Exception e) {
            log.error("密码验证异常", e);
            return false;
        }
    }
}
