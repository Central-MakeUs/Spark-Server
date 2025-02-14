package com.example.spark.domain.youtube.api;

import com.example.spark.domain.youtube.dto.GoogleAuthRequest;
import com.example.spark.global.response.GoogleTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

import java.util.Map;

@Tag(name = "Google OAuth API", description = "Google OAuth 2.0 인증 및 토큰 처리 API")
@RestController
public class OAuthController {

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
    public ResponseEntity<GoogleTokenResponse> exchangeCodeForToken(@RequestBody GoogleAuthRequest request) {
        String code = request.getCode();

        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest().body(new GoogleTokenResponse("Error", "Authorization code is missing"));
        }

        // 요청 파라미터 생성 (Body 포함)
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
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
            ResponseEntity<GoogleTokenResponse> response = restTemplate.postForEntity(
                    TOKEN_ENDPOINT, requestEntity, GoogleTokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                GoogleTokenResponse tokenResponse = response.getBody();

                return ResponseEntity.ok().body(tokenResponse);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body(new GoogleTokenResponse("Error", "Failed to retrieve access token"));
            }
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new GoogleTokenResponse("Error", "Unexpected error occurred: " + ex.getMessage()));
        }
    }
}

