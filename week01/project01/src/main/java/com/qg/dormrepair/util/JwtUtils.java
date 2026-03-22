// util/JwtUtils.java
package com.qg.dormrepair.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;
/**
 * JWT 工具类
 */
@Slf4j
public class JwtUtils {
    // 密钥
    private static String signKey = "SVRIRUlNQQ==";

    // 必须是 final，防止被修改
    private static final Long expire = 86400000L;

    /**
     * 生成 JWT
     * @param claims
     * @return
     */
    public static String generateJwt(Map<String, Object> claims) {
        long currentTime = System.currentTimeMillis();
        long expireTime = currentTime + expire;

        String jwt = Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(expireTime))
                .compact();

        return jwt;
    }
    /**
     * 解析 JWT
     * @param jwt
     * @return
     */
    public static Claims parseJWT(String jwt) {
        Claims claims = Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(jwt)
                .getBody();
        return claims;
    }
}
