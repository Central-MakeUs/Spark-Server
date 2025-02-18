package com.example.spark.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ErrorResponse<T>(
        @Schema(example = "400") int statusCode,
        @Schema(example = "요청이 잘못되었습니다.") String message,
        T errorDetails) {

    public static <T> ErrorResponse<T> badRequest(T errorDetails) {
        return new ErrorResponse<>(400, "요청이 잘못되었습니다.", errorDetails);
    }

    public static <T> ErrorResponse<T> unauthorized(T errorDetails) {
        return new ErrorResponse<>(401, "인증이 필요합니다.", errorDetails);
    }

    public static <T> ErrorResponse<T> forbidden(T errorDetails) {
        return new ErrorResponse<>(403, "접근 권한이 없습니다.", errorDetails);
    }

    public static <T> ErrorResponse<T> notFound(T errorDetails) {
        return new ErrorResponse<>(404, "요청한 리소스를 찾을 수 없습니다.", errorDetails);
    }

    public static <T> ErrorResponse<T> internalServerError(T errorDetails) {
        return new ErrorResponse<>(500, "서버 내부 오류가 발생했습니다.", errorDetails);
    }
}
