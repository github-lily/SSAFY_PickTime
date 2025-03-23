package com.b101.pickTime.common.exception.handler;

import com.b101.pickTime.api.ApiResponseDto;
import com.b101.pickTime.common.exception.exception.DuplicateEmailException;
import com.b101.pickTime.common.exception.exception.FailedSendEmailException;
import com.b101.pickTime.common.exception.exception.InvalidRefreshTokenException;
import com.b101.pickTime.common.exception.exception.NotFoundUsernameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> handleDuplicateEmailException(DuplicateEmailException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFoundException(UsernameNotFoundException ex) {
//        ApiResponseDto<?> response = new ApiResponseDto<>(
//                HttpStatus.NOT_FOUND.value(),  // 404
//                ex.getMessage(),
//                null
//        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<?> invalidRefreshTokenException(DuplicateEmailException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
    @ExceptionHandler(FailedSendEmailException.class)
    public ResponseEntity<?> failedSendEmailException(DuplicateEmailException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
    @ExceptionHandler(NotFoundUsernameException.class)
    public ResponseEntity<?> notFoundUsernameException(NotFoundUsernameException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

}
