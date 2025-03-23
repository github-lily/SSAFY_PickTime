package com.b101.pickTime.common.exception.handler;

import com.b101.pickTime.common.exception.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public ResponseEntity<?> invalidRefreshTokenException(InvalidRefreshTokenException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
    @ExceptionHandler(FailedSendEmailException.class)
    public ResponseEntity<?> failedSendEmailException(FailedSendEmailException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
    @ExceptionHandler(NotFoundUsernameException.class)
    public ResponseEntity<?> notFoundUsernameException(NotFoundUsernameException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
    @ExceptionHandler(PasswordNotMatchedException.class)
    public ResponseEntity<?> passwordNotMatchedException(PasswordNotMatchedException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }
    @ExceptionHandler(PasswordNotChangedException.class)
    public ResponseEntity<?> passwordNotChangedException(PasswordNotChangedException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
    }

}
