package com.example.spark.global.error;

import com.example.spark.global.response.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
        log.error("CustomException 발생: {}", ex.getMessage(), ex);
        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.fromErrorCode(ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("🔥 전역 Exception 발생: {}", ex.getMessage(), ex); // 이걸로 찍기
        return ResponseEntity.status(ErrorCode.UNEXPECTED_ERROR.getStatus())
                .body(ErrorResponse.fromErrorCode(ErrorCode.UNEXPECTED_ERROR));
    }
}
