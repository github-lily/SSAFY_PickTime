package com.b101.pickTime.common.exception.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotFoundUsernameException extends RuntimeException {
    private final int status;
    private final String message;

    public NotFoundUsernameException() {
        this.message = "username is not exist.";
        this.status = HttpStatus.BAD_REQUEST.value(); // 400 상태 코드 설정
    }
}
