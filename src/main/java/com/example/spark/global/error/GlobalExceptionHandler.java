package com.example.spark.global.error;

import com.example.spark.global.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex, HttpServletRequest request) {
        log.error("CustomException 발생: {}", ex.getMessage(), ex);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", request.getHeader("Origin"));
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", "*");

        return ResponseEntity.status(ex.getErrorCode().getStatus())
                .headers(headers)
                .body(ErrorResponse.fromErrorCode(ex.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("🔥 전역 Exception 발생: {}", ex.getMessage(), ex);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Access-Control-Allow-Origin", request.getHeader("Origin"));
        headers.add("Access-Control-Allow-Credentials", "true");
        headers.add("Access-Control-Allow-Headers", "*");

        return ResponseEntity.status(ErrorCode.UNEXPECTED_ERROR.getStatus())
                .headers(headers)
                .body(ErrorResponse.fromErrorCode(ErrorCode.UNEXPECTED_ERROR));
    }
}

