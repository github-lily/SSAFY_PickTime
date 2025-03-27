package com.b101.pickTime.common.auth;

import com.b101.pickTime.common.util.JWTUtil;
import com.b101.pickTime.db.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

// access 토큰을 검증하는 클래스
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 요청 헤더에 담긴 access키의 토큰을 꺼냄
        String accessToken = request.getHeader("Authorization");

        // 토큰이 없다면 다음 필터로 넘김
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 만료 여부 확인
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {   // 만료 시 에러 발생 -> 다시 refresh 토큰 발급 요청 보내도록 에러를 반환
            responseWrite("access Token expired", response);
            return;
        }

        // 토큰이 access인지 확인
        String category = jwtUtil.getCategory(accessToken);

        if (!category.equals("access")) {
            responseWrite("Token is not access token", response);
            return;
        }
    
        // access 토큰 확인 완료
        Integer userId = jwtUtil.getUserId(accessToken);
        String username = jwtUtil.getUsername(accessToken);
        String role = jwtUtil.getRole(accessToken);

        CustomUserDetails customUserDetails = new CustomUserDetails(userId, username, "", role);

        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
    private void responseWrite(String message, HttpServletResponse response) throws IOException {
        PrintWriter writer = response.getWriter();
        writer.println(message);
        // response status code
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
}
