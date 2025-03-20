package com.b101.pickTime.api.user.controller;

import com.b101.pickTime.api.user.service.ReissueService;
import com.b101.pickTime.api.user.service.ReissueServiceImpl;
import com.b101.pickTime.common.util.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reissue")
@RequiredArgsConstructor
public class ReissueController {
    private final JWTUtil jwtUtil;
    private final ReissueService reissueService;

    // refresh토큰을 확인해 액세스토큰 재발급
   @PostMapping
   public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String newAccessToken = reissueService.reissue(request);
        response.setHeader("access", newAccessToken);
        return ResponseEntity.status(HttpStatus.OK).body("new access token is reissued successfully");
   }
}
