package com.b101.pickTime.common.exception.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class DuplicateEmailException extends RuntimeException {
    private final int status;
    private final String message;

    public DuplicateEmailException() {
        this.message = "이미 존재하는 이메일입니다.";
        this.status = HttpStatus.CONFLICT.value(); // 400 상태 코드 설정
    }
}
