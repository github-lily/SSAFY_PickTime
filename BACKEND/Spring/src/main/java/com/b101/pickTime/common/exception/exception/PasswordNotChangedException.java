package com.b101.pickTime.common.exception.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PasswordNotChangedException extends RuntimeException{
    private final int status;
    private final String message;

    public PasswordNotChangedException(String message) {
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST.value();
    }
}
