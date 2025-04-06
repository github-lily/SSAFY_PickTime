package com.b101.pickTime.api.user.controller;

import com.b101.pickTime.api.user.request.UserModiftReqDto;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.response.UserInfoDto;
import com.b101.pickTime.api.user.request.*;
import com.b101.pickTime.api.user.service.UserService;
import com.b101.pickTime.common.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
//@RequestMapping("/api/user")  // yaml에서 한번에 처리할 것임
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    // 회원가입
    @PostMapping
//    public ApiResponseDto<?> singUp(@RequestBody UserRegisterReq userRegisterReq) {
    public ResponseEntity<String> singUp(@RequestBody @Valid UserRegisterReq userRegisterReq) {
        userService.createUser(userRegisterReq);

        return ResponseEntity.ok("User created success");
//        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
//                HttpStatus.CREATED.value(),
//                "User created success",
//                null
//        ));
    }


    @GetMapping
    public ResponseEntity<UserInfoDto> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        UserInfoDto userInfo = userService.getUser(customUserDetails.getUserId());
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping
    public ResponseEntity<UserInfoDto> modifyUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      @RequestBody UserModiftReqDto userModiftReqDto){
        UserInfoDto userInfo = userService.modifyUser(customUserDetails.getUserId(), userModiftReqDto);
        return ResponseEntity.ok(userInfo);
    }

    @DeleteMapping
    public ResponseEntity<String> unactivateUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
       userService.unactivateUser(customUserDetails.getUserId());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    // 비밀번호 확인
    @PostMapping("/password")
    public ResponseEntity<?> checkPassword(@RequestBody PasswordCheckReq passwordCheckReq, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        userService.checkPassword(passwordCheckReq, customUserDetails.getUserId());

        return ResponseEntity.ok("password is correct");
    }

    // 비밀번호 수정
    @PatchMapping("/password")
    public ResponseEntity<?> checkPassword(@RequestBody PasswordUpdateReq passwordCheckReq, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        userService.modifyPassword(passwordCheckReq, customUserDetails.getUserId());

        return ResponseEntity.ok("password is updated");
    }
}
