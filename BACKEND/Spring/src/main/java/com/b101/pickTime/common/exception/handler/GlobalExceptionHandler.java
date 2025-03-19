package com.b101.pickTime.common.exception.handler;

import com.b101.pickTime.api.ApiResponseDto;
import com.b101.pickTime.common.exception.exception.DuplicateEmailException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
//    @ExceptionHandler(DuplicateEmailException.class)
//    public ApiResponseDto<?> handleDuplicateEmailException(DuplicateEmailException ex) {
//        return new ApiResponseDto<>(
//                ex.getStatus(),
//                ex.getMessage(),
//                null
//        );
//    }
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDuplicateEmailException(DuplicateEmailException ex) {
        ApiResponseDto<?> response = new ApiResponseDto<>(
                ex.getStatus(),  // 409 Conflict 상태 코드
                ex.getMessage(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(response); // ✅ 실제 HTTP 응답 코드 설정
    }
}
