package com.example.spark.global.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    @Schema(description = "Google Authorization Code", example = "4/0ASVgi3KyVjpomtvUkkR-UFNZti0...")
    private String code;
}
