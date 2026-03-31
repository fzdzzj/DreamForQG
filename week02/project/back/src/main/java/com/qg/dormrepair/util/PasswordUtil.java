package com.qg.dormrepair.util;

import com.qg.dormrepair.constants.MessageConstant;
import lombok.extern.slf4j.Slf4j;

import java.security.MessageDigest;

/**
 * 密码加密工具类
 * 提供基于 MD5 的密码加密与密码比对功能
 * 注意：MD5 安全性较低，生产环境使用 BCrypt
 */
@Slf4j
public class PasswordUtil {

    /**
     * 私有构造器，防止工具类被实例化
     */
    private PasswordUtil() {
    }

    /**
     * 使用 MD5 算法加密明文密码
     *
     * @param password 明文密码
     * @return 32位小写十六进制加密字符串
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
            throw new RuntimeException(MessageConstant.PASSWORD_ENCRYPT_ERROR, e);
        }
    }

    /**
     * 校验明文密码与加密密码是否一致
     *
     * @param rawPassword 明文密码
     * @param encryptedPassword 数据库中存储的加密密码
     * @return 一致返回 true，不一致返回 false
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