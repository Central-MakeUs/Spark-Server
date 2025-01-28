package com.example.spark.domain.youtube.service;

import com.example.spark.domain.youtube.dto.YouTubeChannelProfileDto;
import com.example.spark.domain.youtube.dto.YouTubeVideoDto;
import com.example.spark.domain.youtube.dto.YouTubeAnalyticsResponse;
import com.example.spark.domain.youtube.dto.YouTubeApiResponse;
import com.example.spark.domain.youtube.dto.YouTubeChannelProfileResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.stream.Collectors;


import java.util.List;

@Service
public class YouTubeService {

    private final RestTemplate restTemplate;

    public YouTubeService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    /**
     * 채널 프로필 정보를 가져오는 메서드
     */
    public YouTubeChannelProfileDto getChannelProfile(String accessToken) {
        String channelApiUrl = "https://www.googleapis.com/youtube/v3/channels"
                + "?part=snippet,statistics"
                + "&mine=true";

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 채널 API 호출
        ResponseEntity<YouTubeChannelProfileResponse> response = restTemplate.exchange(
                channelApiUrl,
                HttpMethod.GET,
                entity,
                YouTubeChannelProfileResponse.class
        );
        YouTubeChannelProfileResponse channelResponse = response.getBody();

        if (channelResponse == null || channelResponse.getItems() == null || channelResponse.getItems().isEmpty()) {
            throw new RuntimeException("YouTube Data API 응답이 비어 있거나 유효하지 않습니다.");
        }

        // 첫 번째 채널 데이터 가져오기
        YouTubeChannelProfileResponse.Item channelData = channelResponse.getItems().get(0);

        // DTO로 변환하여 반환
        return new YouTubeChannelProfileDto(
                channelData.getId(),
                channelData.getSnippet().getTitle(),
                channelData.getStatistics().getVideoCount(),
                channelData.getStatistics().getSubscriberCount(),
                channelData.getStatistics().getViewCount()
        );
    }
    /**
     * 조회수 상위 비디오 ID 가져오기
     */
    private List<String> getTopVideoIds(String accessToken, String channelId) {
        String analyticsApiUrl = "https://youtubeanalytics.googleapis.com/v2/reports"
                + "?ids=channel==%s"
                + "&dimensions=video"
                + "&metrics=views"
                + "&startDate=1970-01-01"
                + "&endDate=2025-01-15"
                + "&sort=-views"
                + "&maxResults=3";
        analyticsApiUrl = String.format(analyticsApiUrl, channelId);


        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<YouTubeAnalyticsResponse> response = restTemplate.exchange(
                analyticsApiUrl,
                HttpMethod.GET,
                entity,
                YouTubeAnalyticsResponse.class
        );

        YouTubeAnalyticsResponse analyticsResponse = response.getBody();


        if (analyticsResponse == null || analyticsResponse.getRows() == null) {
            throw new RuntimeException("YouTube Analytics API 응답이 비어 있습니다.");
        }

        // videoId 목록 반환
        List<String> videoIds = analyticsResponse.getRows()
                .stream()
                .map(row -> row.get(0)) // 첫 번째 열이 videoId
                .collect(Collectors.toList());

        return videoIds;
    }

    /**
     * 비디오 ID로 상세 정보 가져오기
     */
    private List<YouTubeVideoDto> getVideoDetails(String accessToken, List<String> videoIds) {
        String videoApiUrl = "https://www.googleapis.com/youtube/v3/videos"
                + "?part=snippet,statistics"
                + "&fields=items(id,snippet(title,publishedAt),statistics(viewCount))"
                + "&id=" + String.join(",", videoIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<YouTubeApiResponse> response = restTemplate.exchange(
                videoApiUrl,
                HttpMethod.GET,
                entity,
                YouTubeApiResponse.class
        );

        YouTubeApiResponse videoResponse = response.getBody();

        if (videoResponse == null || videoResponse.getItems() == null) {
            throw new RuntimeException("YouTube Data API 응답이 비어 있습니다.");
        }

        // 응답 데이터를 DTO로 변환
        List<YouTubeVideoDto> videoDetails = videoResponse.getItems().stream()
                .map(item -> new YouTubeVideoDto(
                        item.getId(),
                        new YouTubeVideoDto.Snippet(
                                item.getSnippet().getTitle(),
                                item.getSnippet().getPublishedAt()
                        ),
                        new YouTubeVideoDto.Statistics(
                                item.getStatistics().getViewCount()
                        )
                ))
                .toList();

        return videoDetails;
    }

    /**
     * 상위 비디오 데이터를 가져오는 메인 메서드
     */
    public List<YouTubeVideoDto> getTopVideos(String accessToken, String channelId) {

        // Step 1: 조회수 상위 비디오 ID 가져오기
        List<String> videoIds = getTopVideoIds(accessToken, channelId);

        // Step 2: 비디오 ID로 상세 정보 가져오기
        List<YouTubeVideoDto> topVideos = getVideoDetails(accessToken, videoIds);

        return topVideos;
    }

}
