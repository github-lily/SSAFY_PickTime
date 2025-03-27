package com.b101.pickTime.api.user.service.impl;

import com.b101.pickTime.api.user.service.ReissueService;
import com.b101.pickTime.common.exception.exception.InvalidRefreshTokenException;
import com.b101.pickTime.common.util.JWTUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReissueServiceImpl implements ReissueService {
    private final JWTUtil jwtUtil;


    // refresh 확인하여 access토큰 재발급
    @Override
    public String reissue(HttpServletRequest request) {
        String refresh = null;
        
        // 쿠키에 담긴 refresh 토큰을 탐색
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refresh")) {
                refresh = cookie.getValue();
            }
        }

        // refresh토큰이 비어있는지 체크
        if (refresh == null) {
            throw new InvalidRefreshTokenException("refresh token is null");
        }
        // 만료 여부 체크
        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            throw new InvalidRefreshTokenException("refresh token is expired");
        }

        // refresh토큰인지 확인
        String category = jwtUtil.getCategory(refresh);
        if (!category.equals("refresh")) {
            throw new InvalidRefreshTokenException("token is not a refresh token");
        }

        // 발급
        Integer userId = jwtUtil.getUserId(refresh);
        String username = jwtUtil.getUsername(refresh);
        String role = jwtUtil.getRole(refresh);

        // 토큰 생성하여 반환
        return jwtUtil.createJwt("access", userId, username, role, JWTUtil.ACCESS_TOKEN_VALIDITY_TIME);
    }
}
