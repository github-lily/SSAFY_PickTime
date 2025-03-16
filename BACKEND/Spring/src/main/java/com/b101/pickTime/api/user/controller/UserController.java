package com.b101.pickTime.api.user.controller;

import com.b101.pickTime.api.ApiResponseDto;
import com.b101.pickTime.api.user.request.UserRegisterReq;
import com.b101.pickTime.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;


    // 회원가입
    @PostMapping("")
    public ResponseEntity<ApiResponseDto<?>> singUp(@RequestBody UserRegisterReq userRegisterReq) {
        userService.createUser(userRegisterReq);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto<>(
                HttpStatus.CREATED.value(),
                "User created success",
                null
        ));
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


}
