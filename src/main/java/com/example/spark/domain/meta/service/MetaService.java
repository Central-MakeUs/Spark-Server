package com.example.spark.domain.meta.service;

import com.example.spark.domain.meta.dto.MetaProfileDto;
import com.example.spark.domain.meta.dto.MetaContentDto;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
                profile.get("media_count").asLong(),
                igUserId);
    }

    // Instagram 상위 컨텐츠 조회
    public List<MetaContentDto> getTopContents(String accessToken, String instagramBusinessAccountId) {
        if (accessToken == null || accessToken.isEmpty()) {
            throw new IllegalArgumentException("Access token이 존재하지 않습니다.");
        }

        if (instagramBusinessAccountId == null || instagramBusinessAccountId.isEmpty()) {
            throw new IllegalArgumentException("Instagram 비즈니스 계정 ID가 존재하지 않습니다.");
        }

        // 공통 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Instagram 미디어 조회 (조회수 포함)
        String mediaApiUrl = "https://graph.facebook.com/v22.0/" + instagramBusinessAccountId 
                + "/media?fields=id,caption,media_type,media_url,timestamp,thumbnail_url,insights.metric(views)";

        ResponseEntity<JsonNode> mediaResponse = restTemplate.exchange(
                mediaApiUrl, HttpMethod.GET, entity, JsonNode.class);

        JsonNode mediaData = mediaResponse.getBody().get("data");

        if (mediaData == null) {
            throw new RuntimeException("Instagram 미디어 데이터를 가져오지 못했습니다.");
        }

        List<MetaContentDto> contents = new ArrayList<>();

        for (JsonNode media : mediaData) {
            String id = media.get("id").asText();
            String caption = media.has("caption") ? media.get("caption").asText() : "";
            String timestamp = media.get("timestamp").asText();
            String mediaType = media.get("media_type").asText();
            
            // media_type에 따라 적절한 URL 선택
            String contentUrl = "";
            if ("VIDEO".equals(mediaType)) {
                // 비디오인 경우 thumbnail_url 사용
                contentUrl = media.has("thumbnail_url") ? media.get("thumbnail_url").asText() : "";
            } else {
                // 이미지인 경우 media_url 사용
                contentUrl = media.has("media_url") ? media.get("media_url").asText() : "";
            }
            
            Long views = 0L;
            if (media.has("insights") && media.get("insights").has("data")) {
                JsonNode insights = media.get("insights").get("data");
                for (JsonNode insight : insights) {
                    if ("views".equals(insight.get("name").asText())) {
                        views = insight.get("values").get(0).get("value").asLong();
                        break;
                    }
                }
            }

            contents.add(MetaContentDto.builder()
                    .id(id)
                    .caption(caption)
                    .timestamp(timestamp)
                    .mediaType(mediaType)
                    .contentUrl(contentUrl)
                    .views(views)
                    .build());
        }

        // 조회수 기준으로 정렬하고 상위 3개만 반환
        return contents.stream()
                .sorted(Comparator.comparing(MetaContentDto::getViews).reversed())
                .limit(3)
                .toList();
    }
}
