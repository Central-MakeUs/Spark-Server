package com.example.spark.domain.youtube.service;

import com.example.spark.domain.youtube.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.spark.global.util.DateRange;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import static com.example.spark.global.util.Util.calculateDateRanges;
import static com.example.spark.global.util.Util.createHttpEntity;


@Service
public class YouTubeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
                //endDate는 오늘 날짜로 설정
                + "&endDate=" + LocalDate.now()
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
                + "&fields=items(id,snippet(title,publishedAt,thumbnails),statistics(viewCount))"
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
                                item.getSnippet().getPublishedAt(),
                                item.getSnippet().getThumbnails()
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

    private Map<String, LocalDate> getVideoUploadDates(List<String> videoIds, HttpEntity<String> entity) {
        Map<String, LocalDate> videoUploadDates = new HashMap<>();

        if (videoIds.isEmpty()) return videoUploadDates;

        // ✅ YouTube API 요청 URL (여러 개의 비디오 ID를 쉼표로 구분하여 한 번에 조회)
        String videoDetailsUrl = String.format(
                "https://www.googleapis.com/youtube/v3/videos"
                        + "?part=snippet"
                        + "&id=%s",
                String.join(",", videoIds) // ✅ 여러 개의 ID를 쉼표(,)로 연결하여 요청
        );

        ResponseEntity<String> videoResponse = restTemplate.exchange(
                videoDetailsUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (videoResponse.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("YouTube Videos API 요청 실패: " + videoResponse.getStatusCode());
        }

        try {
            JsonNode videoJson = objectMapper.readTree(videoResponse.getBody());
            JsonNode items = videoJson.get("items");

            if (items != null) {
                for (JsonNode item : items) {
                    String videoId = item.get("id").asText();
                    String publishedAt = item.get("snippet").get("publishedAt").asText();
                    LocalDate videoDate = LocalDate.parse(publishedAt.substring(0, 10)); // 날짜 부분만 추출
                    videoUploadDates.put(videoId, videoDate);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("비디오 업로드 날짜 조회 실패: " + e.getMessage());
        }

        return videoUploadDates;
    }

    public YouTubeAnalysisResultDto getCombinedStats(String accessToken, String channelId) {
        List<DateRange> dateRanges = calculateDateRanges();
        HttpEntity<String> entity = createHttpEntity(accessToken);

        List<YouTubeCombinedStatsDto> combinedStatsList = new ArrayList<>();
        Map<String, Integer> uploadStatsMap = getUploadStatsMap(accessToken, channelId, dateRanges);

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

            if (analyticsResponse == null || analyticsResponse.getRows() == null || analyticsResponse.getRows().isEmpty()) {
                throw new RuntimeException("YouTube Analytics API 응답이 비어 있습니다.");
            }

            // 업로드된 영상 개수 가져오기
            int uploadedVideos = uploadStatsMap.getOrDefault(dateRange.getStartDate(), 0);

            // 3개 기간(30일, 60일, 90일)에 대한 데이터 추가
            for (List<String> row : analyticsResponse.getRows()) {
                combinedStatsList.add(YouTubeCombinedStatsDto.of(
                        dateRange.getStartDate(),
                        dateRange.getEndDate(),
                        Long.parseLong(row.get(2)), // views
                        Long.parseLong(row.get(0)), // subscribersGained
                        Long.parseLong(row.get(1)), // subscribersLost
                        Long.parseLong(row.get(3)), // likes
                        Long.parseLong(row.get(4)), // comments
                        Long.parseLong(row.get(5)), // shares
                        Double.parseDouble(row.get(6)), // estimatedRevenue
                        Long.parseLong(row.get(7)), // averageViewDuration
                        uploadedVideos // 업로드된 영상 개수
                ));
            }
        }

        // 성장률 분석 추가
        Map<String, Object> analysisResult = calculateGrowthAndRank(combinedStatsList);

        // YouTubeAnalysisResultDto로 변환하여 반환
        return new YouTubeAnalysisResultDto(
                combinedStatsList, // 기존 통계 데이터
                (Map<String, Double>) analysisResult.get("growthRates"), // 성장률 분석 데이터
                (List<String>) analysisResult.get("strengths"), // 강점
                (List<String>) analysisResult.get("weaknesses") // 약점
        );
    }


    //성장률 계산 및 강/약점 분석 메서드
    private Map<String, Object> calculateGrowthAndRank(List<YouTubeCombinedStatsDto> statsList) {
        if (statsList.size() < 2) {
            throw new RuntimeException("성장률을 계산하기 위해서는 최소 두 개의 기간 데이터가 필요합니다.");
        }

        // 최근 30일 데이터 (기간 마지막 값)
        YouTubeCombinedStatsDto recentStats = statsList.get(0);
        // 30~60일 데이터 (기간 첫 값)
        YouTubeCombinedStatsDto previousStats = statsList.get(1);

        Map<String, Double> growthRates = new HashMap<>();

        // 성장률 계산 공식: (기간 마지막 값 - 기간 첫 값) / 기간 첫 값 * 100
        growthRates.put("views", calculateGrowth(recentStats.getViews(), previousStats.getViews()));
        growthRates.put("netSubscribers", calculateGrowth(recentStats.getNetSubscribers(), previousStats.getNetSubscribers()));
        growthRates.put("likes", calculateGrowth(recentStats.getLikes(), previousStats.getLikes()));
        growthRates.put("comments", calculateGrowth(recentStats.getComments(), previousStats.getComments()));
        growthRates.put("shares", calculateGrowth(recentStats.getShares(), previousStats.getShares()));
        growthRates.put("estimatedRevenue", calculateGrowth(recentStats.getEstimatedRevenue(), previousStats.getEstimatedRevenue()));
        growthRates.put("averageViewDuration", calculateGrowth(recentStats.getAverageViewDuration(), previousStats.getAverageViewDuration()));
        growthRates.put("uploadedVideos", calculateGrowth(recentStats.getUploadedVideos(), previousStats.getUploadedVideos()));

        // 강점/약점 분석
        List<Map.Entry<String, Double>> sortedMetrics = growthRates.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 내림차순 정렬
                .toList();

        List<String> strengths = List.of(sortedMetrics.get(0).getKey(), sortedMetrics.get(1).getKey()); // 성장률 상위 2개
        List<String> weaknesses = List.of(sortedMetrics.get(sortedMetrics.size() - 1).getKey(), sortedMetrics.get(sortedMetrics.size() - 2).getKey()); // 성장률 하위 2개

        // 결과 반환
        Map<String, Object> analysisResult = new HashMap<>();
        analysisResult.put("growthRates", growthRates);
        analysisResult.put("strengths", strengths);
        analysisResult.put("weaknesses", weaknesses);

        return analysisResult;
    }

    // 성장률 계산 함수
    private double calculateGrowth(double recent, double previous) {
        if (previous == 0) {
            return recent == 0 ? 0 : 100.0; // 이전 값도 0이면 0%, 아니면 100%
        }
        return ((recent - previous) / previous) * 100;
    }



    private Map<String, Integer> getUploadStatsMap(String accessToken, String channelId, List<DateRange> dateRanges) {
        HttpEntity<String> entity = createHttpEntity(accessToken);
        Map<String, Integer> uploadStatsMap = new HashMap<>();

        // ✅ `publishedAfter`를 가장 오래된 startDate로 설정하여 정확한 데이터 조회
        String publishedAfter = dateRanges.get(dateRanges.size() - 1).getStartDate();

        String searchApiUrl = String.format(
                "https://www.googleapis.com/youtube/v3/search"
                        + "?part=id"
                        + "&channelId=%s"
                        + "&publishedAfter=%sT00:00:00Z"
                        + "&type=video"
                        + "&maxResults=50",
                channelId,
                publishedAfter
        );

        ResponseEntity<String> response = restTemplate.exchange(
                searchApiUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("YouTube Data API 요청 실패: " + response.getStatusCode());
        }

        List<String> videoIds = new ArrayList<>();
        try {
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            JsonNode items = jsonResponse.get("items");

            if (items != null) {
                for (JsonNode item : items) {
                    videoIds.add(item.get("id").get("videoId").asText());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage());
        }

        Map<String, LocalDate> videoUploadDates = getVideoUploadDates(videoIds, entity);

        for (String videoId : videoUploadDates.keySet()) {
            LocalDate videoDate = videoUploadDates.get(videoId);

            for (DateRange dateRange : dateRanges) {
                LocalDate startDateParsed = LocalDate.parse(dateRange.getStartDate());
                LocalDate endDateParsed = LocalDate.parse(dateRange.getEndDate());

                if ((videoDate.isEqual(startDateParsed) || videoDate.isAfter(startDateParsed)) &&
                        videoDate.isBefore(endDateParsed.plusDays(1))) {
                    uploadStatsMap.compute(dateRange.getStartDate(), (k, v) -> (v == null ? 1 : v + 1)); // ✅ 값 자동 증가
                    break;
                }
            }
        }
        return uploadStatsMap;
    }

}
