package com.b101.pickTime.api.user.controller;

import com.b101.pickTime.api.user.request.CheckVerificationReq;
import com.b101.pickTime.api.user.request.EmailVerificationReq;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.service.UserApplicationService;
import com.b101.pickTime.api.user.service.UserService;
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
//@RequestMapping("/api/user")  // yaml에서 한번에 처리할 것임
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final UserApplicationService userApplicationService;
    private final VerificationService verificationService;

    // 회원가입
    @PostMapping
//    public ApiResponseDto<?> singUp(@RequestBody UserRegisterReq userRegisterReq) {
    public ResponseEntity<String> singUp(@RequestBody UserRegisterReq userRegisterReq) {
        userService.createUser(userRegisterReq);

        return ResponseEntity.ok("User created success");
//        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
//                HttpStatus.CREATED.value(),
//                "User created success",
//                null
//        ));
    }

    @PostMapping("/email-verification")
    public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailVerificationReq emailVerificationReq) {
        userApplicationService.sendVerificationEmail(emailVerificationReq);

        return ResponseEntity.ok("Verification email sent success");
    }
    @PostMapping("/check-verification")
    public ResponseEntity<?> checkVerificationNumber(@RequestBody CheckVerificationReq checkVerificationReq) {
        if (verificationService.checkVerificationNumber(checkVerificationReq)) {
            return ResponseEntity.ok("email verified successfully");
        } else {
            return new ResponseEntity<>("failed to verify email",HttpStatus.UNAUTHORIZED);
        }
    }
}
