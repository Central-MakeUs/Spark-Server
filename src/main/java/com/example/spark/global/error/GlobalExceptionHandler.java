package com.example.spark.global.error;

import com.example.spark.global.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.fromErrorCode(ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ex.printStackTrace(); // 로그 확인용
        return ResponseEntity.status(ErrorCode.UNEXPECTED_ERROR.getStatus())
                .body(ErrorResponse.fromErrorCode(ErrorCode.UNEXPECTED_ERROR));
    }
}
