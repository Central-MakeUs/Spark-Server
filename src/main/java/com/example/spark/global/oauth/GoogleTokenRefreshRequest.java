package com.example.spark.global.oauth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GoogleTokenRefreshRequest {

    @Schema(description = "Google OAuth 2.0에서 받은 Refresh Token", example = "1//0g123abc456xyz789")
    private String refreshToken;
}