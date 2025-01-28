package com.example.spark.domain.youtube.service;

import com.example.spark.domain.youtube.dto.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
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

    public List<YouTubeChannelStatsDto> getChannelStats(String accessToken, String channelId) {
        // 현재 날짜 기준으로 동적으로 날짜 범위 계산
        List<DateRange> dateRanges = calculateDateRanges();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<YouTubeChannelStatsDto> allChannelStats = new ArrayList<>();

        for (DateRange dateRange : dateRanges) {
            String analyticsApiUrl = String.format(
                    "https://youtubeanalytics.googleapis.com/v2/reports"
                            + "?ids=channel==%s"
                            + "&metrics=subscribersGained,subscribersLost,views,likes,comments,shares,estimatedRevenue,averageViewDuration"
                            + "&startDate=%s"
                            + "&endDate=%s",
                    channelId,
                    dateRange.getStartDate(),
                    dateRange.getEndDate()
            );

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

            // 채널 통계 데이터를 변환하여 리스트에 추가
            List<YouTubeChannelStatsDto> channelStats = analyticsResponse.getRows().stream()
                    .map(row -> new YouTubeChannelStatsDto(
                            dateRange.getStartDate(),
                            dateRange.getEndDate(),
                            Long.parseLong(row.get(2)), // views
                            Long.parseLong(row.get(0)), // subscribersGained
                            Long.parseLong(row.get(1)), // subscribersLost
                            Long.parseLong(row.get(3)), // likes
                            Long.parseLong(row.get(4)), // comments
                            Long.parseLong(row.get(5)), // shares
                            Double.parseDouble(row.get(6)), // estimatedRevenue
                            Long.parseLong(row.get(7)) // averageViewDuration
                    ))
                    .toList();

            allChannelStats.addAll(channelStats);
        }

        return allChannelStats;
    }

    // 날짜 범위 계산
    private List<DateRange> calculateDateRanges() {
        LocalDate today = LocalDate.now();
        List<DateRange> dateRanges = new ArrayList<>();

        dateRanges.add(new DateRange(today.minusDays(30).toString(), today.toString())); // 최근 30일
        dateRanges.add(new DateRange(today.minusDays(60).toString(), today.minusDays(30).toString())); // 최근 30~60일
        dateRanges.add(new DateRange(today.minusDays(90).toString(), today.minusDays(60).toString())); // 최근 60~90일

        return dateRanges;
    }

    // 날짜 범위를 저장할 내부 클래스
    private static class DateRange {
        private final String startDate;
        private final String endDate;

        public DateRange(String startDate, String endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
        }
    }

}
