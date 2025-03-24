package com.b101.pickTime.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// JWT 발급과 검증을 담당하는 클래스
@Component
public class JWTUtil {
    private SecretKey secretKey;
    public JWTUtil(@Value("${jwt.auth-key}")String secret) {
        secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }
    public static final long ACCESS_TOKEN_VALIDITY_TIME = 1000 * 60 * 60 * 2L;
    public static final long REFRESH_TOKEN_VALIDITY_TIME = 1000 * 60 * 60 * 3L;
    public static final String BEARER_PREFIX = "Bearer ";

    /**
    토큰 Payload에 저장될 정보 :
        - userId, username, role, 생성일, 만료일
    JWTUtil 구현 메소드 :
        - JWTUtil 생성자, userId, username, role 확인 메소드(각각), 만료일 확인 메소드
     */
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload();
    }
    public Integer getUserId(String token) {
        return getClaims(token).get("userId", Integer.class);
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("userId", Integer.class);
    }
    public String getCategory(String token) {
        return getClaims(token).get("category", String.class);
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("category", String.class);
    }
    // Jwt 검증 메서드
    public String getUsername(String token) {
        return getClaims(token).get("username", String.class);
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("username", String.class);
    }
    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().get("role", String.class);
    }
    public Boolean isExpired(String token) {
        // 만료 시 에러 발생 코드 필요? => 없으면 500에러 발생할 가능성
        return getClaims(token).getExpiration().before(new Date());
//        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
    }

    // Jwt 발급 메서드!
    public String createJwt(String category, Integer userId, String username, String role, Long expiredMs) {
        return Jwts.builder()
                .claim("category", category)    // "access" or "refresh"
                .claim("userId", userId)
                .claim("username", username)
                .claim("role", role)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiredMs))
                .signWith(secretKey)
                .compact();
    }
    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(BEARER_PREFIX.length());
        }
        throw new NullPointerException("Not Found Token");
    }
}
