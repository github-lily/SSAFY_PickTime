package com.b101.pickTime.api.user.controller;

import com.b101.pickTime.api.user.request.UserModiftReqDto;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.response.UserInfoDto;
import com.b101.pickTime.api.user.service.UserService;
import com.b101.pickTime.common.auth.CustomUserDetails;
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
    public ResponseEntity<String> singUp(@RequestBody UserRegisterReq userRegisterReq) {
        userService.createUser(userRegisterReq);

        return ResponseEntity.ok("User created success");
//        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
//                HttpStatus.CREATED.value(),
//                "User created success",
//                null
//        ));
    }
//    @PostMapping("")
//    public ApiResponseDto<?> singUp(@RequestBody UserRegisterReq userRegisterReq) {
//        System.out.println("진입성공");
//        userService.createUser(userRegisterReq);
//        return new ApiResponseDto<>(
//                HttpStatus.CREATED.value(),
//                "User created success",
//                null
//        );
//    }

    @GetMapping
    public ResponseEntity<UserInfoDto> getUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        UserInfoDto userInfo = userService.getUser(customUserDetails.getUserId());
        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping
    public ResponseEntity<UserInfoDto> modifyUserInfo(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                      @RequestBody UserModiftReqDto userModiftReqDto){
        UserInfoDto userInfo = userService.getUser(customUserDetails.getUserId());
        return ResponseEntity.ok(userInfo);
    }

    @DeleteMapping
    public ResponseEntity<String> unactivateUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
       userService.unactivateUser(customUserDetails.getUserId());
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

}
