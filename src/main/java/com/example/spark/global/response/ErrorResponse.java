package com.example.spark.global.response;

import com.example.spark.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String code;
    private final String message;

    public static ErrorResponse fromErrorCode(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
    }
}