package com.example.spark.domain.auth.api;

import com.example.spark.global.error.ErrorCode;
import com.example.spark.global.oauth.AuthRequest;
import com.example.spark.global.oauth.TokenRefreshRequest;
import com.example.spark.global.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Tag(name = "YouTube(Google) - OAuth", description = "Google OAuth 2.0 인증 및 토큰 처리 API")
@RestController
public class GoogleOAuthController {

    @Value("${GOOGLE_CLIENT_ID}")
    private String CLIENT_ID;

    @Value("${GOOGLE_CLIENT_SECRET}")
    private String CLIENT_SECRET;

    @Value("${GOOGLE_REDIRECT_URI}")
    private String REDIRECT_URI;

    private static final String SCOPE = String.join(" ",
            "https://www.googleapis.com/auth/youtube.readonly",
            "https://www.googleapis.com/auth/yt-analytics.readonly",
            "https://www.googleapis.com/auth/yt-analytics-monetary.readonly");
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";

    /**
     * Google 인증 URL 생성
     */
    @Operation(summary = "Google 인증 URL 생성", description = "Google 인증 URL을 생성하여 반환합니다.")
    @GetMapping("/oauth/google/auth")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
                "?response_type=code" +
                "&client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=" + SCOPE +
                "&access_type=offline" +
                "&prompt=consent";

        return ResponseEntity.ok(Map.of("googleAuthUrl", googleAuthUrl));
    }

    /**
     * Authorization Code로 Access Token 교환
     */
    @Operation(summary = "Authorization Code로 Access Token 교환", description = "Google의 Authorization Code를 사용하여 Access Token을 발급받습니다.")
    @PostMapping("/oauth/google/callback")
    public ResponseEntity<TokenResponse> exchangeCodeForToken(@RequestBody AuthRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest().body(new TokenResponse(ErrorCode.INVALID_AUTHORIZATION_CODE));
        }

        // 요청 파라미터 생성 (Body 포함)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", request.getCode());
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("grant_type", "authorization_code");

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // Google 서버로 요청 보내기
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    TOKEN_ENDPOINT, requestEntity, TokenResponse.class);

            return response.getStatusCode().is2xxSuccessful()
                    ? ResponseEntity.ok(response.getBody())
                    : ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenResponse(ErrorCode.ACCESS_TOKEN_EXPIRED));

        } catch (Exception ex) {
            ErrorCode errorCode = ex.getMessage().contains("invalid_grant")
                    ? ErrorCode.INVALID_AUTHORIZATION_CODE
                    : ErrorCode.UNEXPECTED_ERROR;

            return ResponseEntity.status(errorCode.getStatus())
                    .body(new TokenResponse(errorCode));
        }
    }

    @Operation(summary = "Refresh Token을 사용하여 Access Token 갱신", description = "Google의 Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/oauth/google/refresh")
    public ResponseEntity<TokenResponse> refreshAccessToken(@RequestBody TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenResponse(ErrorCode.REFRESH_TOKEN_EXPIRED));
        }

        // 요청 파라미터 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 생성
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        // Google 서버로 요청 보내기
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    TOKEN_ENDPOINT, requestEntity, TokenResponse.class);

            return response.getStatusCode().is2xxSuccessful()
                    ? ResponseEntity.ok(response.getBody())
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new TokenResponse(ErrorCode.INTERNAL_SERVER_ERROR));

        } catch (Exception ex) {
            ErrorCode errorCode = ex.getMessage().contains("invalid_grant") ?
                    ErrorCode.REFRESH_TOKEN_EXPIRED :  // Refresh Token이 만료된 경우
                    ErrorCode.INTERNAL_SERVER_ERROR;   // 기타 예상치 못한 오류는 INTERNAL_SERVER_ERROR 반환

            return ResponseEntity.status(errorCode.getStatus())
                    .body(new TokenResponse(errorCode));
        }
    }
}

