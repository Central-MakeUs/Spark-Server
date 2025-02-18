package com.example.spark.global.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record SuccessResponse<T>(
        @Schema(example = "200", description = "응답 상태 코드") int statusCode,
        @Schema(example = "요청에 성공하였습니다.", description = "응답 메시지") String message,
        @Schema(description = "응답 데이터") T result) {

    public static <T> SuccessResponse<T> success(T result) {
        return new SuccessResponse<>(200, "요청에 성공하였습니다.", result);
    }

    public static <T> SuccessResponse<T> createSuccess(T result) {
        return new SuccessResponse<>(201, "생성에 성공하였습니다.", result);
    }
}
