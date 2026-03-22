// vo/LoginResponse.java
package com.qg.dormrepair.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应 VO
 * 包含用户信息和 JWT Token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 用户信息
     */
    private UserVO user;

    /**
     * JWT 令牌
     */
    private String token;
}
