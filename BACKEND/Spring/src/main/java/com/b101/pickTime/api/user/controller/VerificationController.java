package com.b101.pickTime.api.user.controller;

import com.b101.pickTime.api.user.request.CheckVerificationReq;
import com.b101.pickTime.api.user.request.EmailVerificationReq;
import com.b101.pickTime.api.user.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/verification")
public class VerificationController {
    private final VerificationService verificationService;

    @PostMapping("/email")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailVerificationReq emailVerificationReq) {
        verificationService.sendVerificationEmail(emailVerificationReq);
        return ResponseEntity.ok("Verification email sent success");
    }
    @PostMapping("/check")
    public ResponseEntity<?> checkVerificationNumber(@RequestBody CheckVerificationReq checkVerificationReq) {
        if (verificationService.checkVerificationNumber(checkVerificationReq)) {
            return ResponseEntity.ok("email verified successfully");
        } else {
            return new ResponseEntity<>("failed to verify email", HttpStatus.UNAUTHORIZED);
        }
    }
}