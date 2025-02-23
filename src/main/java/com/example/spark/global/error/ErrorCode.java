package com.example.spark.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "COMMON_008", "Access token expired"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED.value(), "COMMON_012", "Refresh token expired"),
    UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "COMMON_999", "Unexpected error occurred"),
    INVALID_AUTHORIZATION_CODE(HttpStatus.BAD_REQUEST.value(), "COMMON_013", "Invalid authorization code"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "COMMON_999", "Unexpected error occurred");
    private final int status;
    private final String code;
    private final String message;
}
