package com.b101.pickTime.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class ApiResponseDto<T> {
    private int status;
    private String message;
    private T data;
}
