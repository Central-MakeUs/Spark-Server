package com.example.spark.domain.auth.api;

import com.example.spark.global.error.ErrorCode;
import com.example.spark.global.oauth.AuthRequest;
import com.example.spark.global.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Tag(name = "Meta - OAuth", description = "Meta OAuth 2.0 인증 및 토큰 처리 API")
@RestController
public class FacebookOAuthController {
    @Value("${META_CLIENT_ID}")
    private String CLIENT_ID;

    @Value("${META_CLIENT_SECRET}")
    private String CLIENT_SECRET;

    @Value("${META_REDIRECT_URI}")
    private String REDIRECT_URI;

    private static final String TOKEN_ENDPOINT = "https://graph.facebook.com/v22.0/oauth/access_token";

    /**
     * Meta 인증 URL 생성
     */
    @Operation(summary = "Meta 인증 URL 생성", description = "Meta 인증 URL을 생성하여 반환합니다.")
    @GetMapping("/oauth/meta/auth")
    public ResponseEntity<Map<String, String>> getMetaAuthUrl() {
        String metaAuthUrl = "https://www.facebook.com/v22.0/dialog/oauth" +
                "?client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=pages_show_list,ads_read,instagram_basic,instagram_manage_insights,pages_read_engagement" +
                "&response_type=code";

        return ResponseEntity.ok(Map.of("metaAuthUrl", metaAuthUrl));
    }

    /**
     * Authorization Code로 Access Token 교환
     */
    @PostMapping("/oauth/meta/callback")
    @Operation(summary = "Authorization Code로 장기 Access Token 발급", description = "Meta의 Authorization Code를 사용하여 Access Token을 발급받고 장기 토큰으로 교환합니다.")
    public ResponseEntity<TokenResponse> exchangeCodeForToken(@RequestBody AuthRequest request) {
        if (request.getCode() == null || request.getCode().isEmpty()) {
            return ResponseEntity.badRequest().body(new TokenResponse(ErrorCode.INVALID_AUTHORIZATION_CODE));
        }

        try {
            // 1. 단기 Access Token 발급
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", request.getCode());
            params.add("client_id", CLIENT_ID);
            params.add("client_secret", CLIENT_SECRET);
            params.add("redirect_uri", REDIRECT_URI);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            RestTemplate restTemplate = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            ResponseEntity<Map> shortTokenResponse = restTemplate.postForEntity(
                    TOKEN_ENDPOINT, requestEntity, Map.class);

            if (!shortTokenResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenResponse(ErrorCode.ACCESS_TOKEN_EXPIRED));
            }

            String shortToken = (String) shortTokenResponse.getBody().get("access_token");

            // 2. 장기 Access Token으로 교환
            String exchangeUrl = UriComponentsBuilder
                    .fromHttpUrl(TOKEN_ENDPOINT)
                    .queryParam("grant_type", "fb_exchange_token")
                    .queryParam("client_id", CLIENT_ID)
                    .queryParam("client_secret", CLIENT_SECRET)
                    .queryParam("fb_exchange_token", shortToken)
                    .toUriString();

            ResponseEntity<Map> longTokenResponse = restTemplate.getForEntity(exchangeUrl, Map.class);

            if (!longTokenResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new TokenResponse(ErrorCode.ACCESS_TOKEN_EXPIRED));
            }

            Map<String, Object> tokenMap = longTokenResponse.getBody();

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken((String) tokenMap.get("access_token"));
            tokenResponse.setExpiresIn((Integer) tokenMap.get("expires_in"));
            tokenResponse.setTokenType((String) tokenMap.get("token_type")); // 대부분 "bearer"
            tokenResponse.setScope(null); // Meta는 scope를 응답하지 않음
            tokenResponse.setRefreshToken(null); // Meta는 refresh_token 없음

            return ResponseEntity.ok(tokenResponse);

        } catch (Exception ex) {
            ex.printStackTrace();
            ErrorCode errorCode = ex.getMessage().contains("invalid_grant")
                    ? ErrorCode.INVALID_AUTHORIZATION_CODE
                    : ErrorCode.UNEXPECTED_ERROR;
            return ResponseEntity.status(errorCode.getStatus())
                    .body(new TokenResponse(errorCode));
        }
    }

}
