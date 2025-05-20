package com.example.spark.domain.meta.service;

import com.example.spark.domain.meta.dto.MetaProfileDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MetaService {
    private final RestTemplate restTemplate;

    public MetaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    //사용자 프로필 조회
    public MetaProfileDto getAccountProfile(String accessToken) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token이 존재하지 않습니다.");
        }

        // 공통 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 1단계: 연결된 페이지에서 Instagram 비즈니스 계정 ID 조회
        String pageUrl = "https://graph.facebook.com/v22.0/me/accounts?fields=id,name,instagram_business_account";

        ResponseEntity<JsonNode> pageResponse = restTemplate.exchange(
                pageUrl, HttpMethod.GET, entity, JsonNode.class);

        JsonNode accounts = pageResponse.getBody().get("data");

        if (accounts == null || !accounts.elements().hasNext()) {
            throw new RuntimeException("연결된 페이지가 없거나 Instagram 계정이 연결되어 있지 않습니다.");
        }

        JsonNode account = accounts.get(0); // 첫 페이지만 사용
        JsonNode instagramAccount = account.get("instagram_business_account");

        if (instagramAccount == null || instagramAccount.get("id") == null) {
            throw new RuntimeException("Instagram 비즈니스 계정이 이 페이지에 연결되어 있지 않습니다.");
        }

        String igUserId = instagramAccount.get("id").asText();

        // 2단계: Instagram 비즈니스 계정 프로필 정보 조회
        String profileUrl = "https://graph.facebook.com/v22.0/" + igUserId
                + "?fields=username,profile_picture_url,followers_count,follows_count,media_count";

        ResponseEntity<JsonNode> profileResponse = restTemplate.exchange(
                profileUrl, HttpMethod.GET, entity, JsonNode.class);

        JsonNode profile = profileResponse.getBody();

        if (profile == null) {
            throw new RuntimeException("Instagram 프로필 정보를 가져오지 못했습니다.");
        }

        return new MetaProfileDto(
                profile.get("username").asText(),
                profile.get("profile_picture_url").asText(),
                profile.get("followers_count").asLong(),
                profile.get("follows_count").asLong(),
                profile.get("media_count").asLong());
    }
}
