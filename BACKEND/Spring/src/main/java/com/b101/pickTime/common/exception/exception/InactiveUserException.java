package com.b101.pickTime.common.exception.exception;

import org.springframework.http.HttpStatus;

public class InactiveUserException extends RuntimeException {
    public InactiveUserException(String message) {
        super(message);
    }

}