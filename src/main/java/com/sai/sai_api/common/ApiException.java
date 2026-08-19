package com.sai.sai_api.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 비즈니스 로직에서 던지는 공통 예외
// 예: throw new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", "유저를 찾을 수 없어요.");
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
