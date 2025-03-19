package com.b101.pickTime.common.exception.exception;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class InvalidRefreshTokenException extends RuntimeException {
    private final int status;
    private final String message;

    public InvalidRefreshTokenException(String message) {
        this.message = message;
        this.status = HttpStatus.BAD_REQUEST.value();
    }
}
