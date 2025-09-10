package com.example.spark.domain.meta.service;

import com.example.spark.domain.meta.dto.MetaProfileDto;
import com.example.spark.domain.meta.dto.MetaContentDto;
import com.example.spark.domain.meta.dto.MetaStatsDto;
import com.example.spark.domain.meta.dto.MetaAnalysisResultDto;
import com.example.spark.global.util.DateRange;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.spark.global.util.Util.calculateDateRanges;

@Slf4j
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
                + "/media?fields=id,caption,media_type,media_url,timestamp,thumbnail_url,insights.metric(views, likes)";

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
            Long likes = 0L;
            if (media.has("insights") && media.get("insights").has("data")) {
                JsonNode insights = media.get("insights").get("data");
                for (JsonNode insight : insights) {
                    String name = insight.get("name").asText();
                    if ("views".equals(name)) {
                        views = insight.get("values").get(0).get("value").asLong();
                    } else if ("likes".equals(name)) {
                        likes = insight.get("values").get(0).get("value").asLong();
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
                    .likes(likes)   // 추가
                    .build());
        }

        // 조회수 기준으로 정렬하고 상위 3개만 반환
        return contents.stream()
                .sorted(Comparator.comparing(MetaContentDto::getViews).reversed())
                .limit(3)
                .toList();
    }

    // Instagram 통계 데이터 조회
    public MetaAnalysisResultDto getCombinedStats(String accessToken, String instagramBusinessAccountId) {
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

        // 날짜 범위 계산
        List<DateRange> dateRanges = calculateDateRanges();
        List<MetaStatsDto> statsList = new ArrayList<>();

        // 1. 광고 계정 ID 조회
        String adAccountId = getAdAccountId(accessToken);

        for (DateRange dateRange : dateRanges) {
            // 2. Instagram 인사이트 데이터 조회
            Map<String, Long> insightsData = getInstagramInsights(accessToken, instagramBusinessAccountId, dateRange);
            
            // 3. 조회수 데이터 조회 (followers/non-followers 구분)
            Map<String, Long> viewsData = getViewsData(accessToken, instagramBusinessAccountId, dateRange);
            
            // 4. 광고 데이터 조회
            Long adsCount = getAdsCount(accessToken, adAccountId, dateRange);
            
            // 5. 업로드된 미디어 데이터 조회
            Map<String, Long> uploadData = getUploadData(accessToken, instagramBusinessAccountId, dateRange);

            MetaStatsDto stats = MetaStatsDto.builder()
                    .startDate(dateRange.getStartDate())
                    .endDate(dateRange.getEndDate())
                    .impressions(insightsData.get("impressions"))
                    .likes(insightsData.get("likes"))
                    .comments(insightsData.get("comments"))
                    .profileStats(insightsData.get("profileStats"))
                    .followers(insightsData.get("followers"))
                    .unfollowers(insightsData.get("unfollowers"))
                    .viewsFollowers(viewsData.get("followers"))
                    .viewsNonFollowers(viewsData.get("nonFollowers"))
                    .adsCount(adsCount)
                    .uploadedMedia(uploadData.get("total"))
                    .build();

            statsList.add(stats);
        }

        // 성장률 계산
        Map<String, Double> growthRates = calculateGrowthRates(statsList);
        
        // 강점/약점 분석
        List<String> strengths = analyzeStrengths(statsList);
        List<String> weaknesses = analyzeWeaknesses(statsList);

        return MetaAnalysisResultDto.builder()
                .stats(statsList)
                .growthRates(growthRates)
                .strengths(strengths)
                .weaknesses(weaknesses)
                .build();
    }

    // 광고 계정 ID 조회
    private String getAdAccountId(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String adAccountsUrl = "https://graph.facebook.com/v22.0/me/adaccounts?fields=id";

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                adAccountsUrl, HttpMethod.GET, entity, JsonNode.class);

        JsonNode data = response.getBody().get("data");
        if (data != null && data.size() > 0) {
            return data.get(0).get("id").asText();
        }
        return null;
    }

    // Instagram 인사이트 데이터 조회
    private Map<String, Long> getInstagramInsights(String accessToken, String instagramBusinessAccountId, DateRange dateRange) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Unix timestamp로 변환
        long since = LocalDate.parse(dateRange.getStartDate()).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
        long until = LocalDate.parse(dateRange.getEndDate()).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);

        String insightsUrl = String.format(
                "https://graph.facebook.com/v22.0/%s/insights?metric=likes,comments,saves,shares,profile_views,profile_links_taps&follows_and_unfollows&period=day&metric_type=total_value&since=%d&until=%d",
                instagramBusinessAccountId, since, until);

        log.info("Instagram Insights API URL: {}", insightsUrl);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                insightsUrl, HttpMethod.GET, entity, JsonNode.class);

        log.info("Instagram Insights API Response: {}", response.getBody());

        Map<String, Long> result = new HashMap<>();
        result.put("impressions", 0L);
        result.put("likes", 0L);
        result.put("comments", 0L);
        result.put("profileStats", 0L);
        result.put("followers", 0L);
        result.put("unfollowers", 0L);

        JsonNode data = response.getBody().get("data");
        if (data != null) {
            log.info("Instagram Insights Data Size: {}", data.size());
            for (JsonNode insight : data) {
                String name = insight.get("name").asText();
                log.info("Processing insight: {}", name);
                
                // total_value 필드에서 값 가져오기
                JsonNode totalValue = insight.get("total_value");
                if (totalValue == null || !totalValue.has("value")) {
                    log.warn("No total_value found for insight: {}", name);
                    continue;
                }
                
                long value = totalValue.get("value").asLong();
                log.info("Insight {} value: {}", name, value);

                switch (name) {
                    case "likes":
                        result.put("likes", value);
                        result.put("impressions", result.get("impressions") + value);
                        break;
                    case "comments":
                        result.put("comments", value);
                        result.put("impressions", result.get("impressions") + value);
                        break;
                    case "saves":
                        result.put("saves", value);
                        result.put("impressions", result.get("impressions") + value);
                        break;
                    case "shares":
                        result.put("shares", value);
                        result.put("impressions", result.get("impressions") + value);
                        break;
                    case "profile_views":
                    case "profile_links_taps":
                        result.put("profileStats", result.get("profileStats") + value);
                        break;
                    case "follows":
                        result.put("followers", value);
                        break;
                    case "unfollows":
                        result.put("unfollowers", value);
                        break;
                }
            }
        } else {
            log.warn("No data found in Instagram Insights response");
        }

        log.info("Final Instagram Insights result: {}", result);
        return result;
    }

    // 조회수 데이터 조회 (followers/non-followers 구분)
    private Map<String, Long> getViewsData(String accessToken, String instagramBusinessAccountId, DateRange dateRange) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        long since = LocalDate.parse(dateRange.getStartDate()).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);
        long until = LocalDate.parse(dateRange.getEndDate()).atStartOfDay().toEpochSecond(java.time.ZoneOffset.UTC);

        String viewsUrl = String.format(
                "https://graph.facebook.com/v22.0/%s/insights?metric=views&period=day&breakdown=follow_type&metric_type=total_value&since=%d&until=%d",
                instagramBusinessAccountId, since, until);

        log.info("Instagram Views API URL: {}", viewsUrl);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                viewsUrl, HttpMethod.GET, entity, JsonNode.class);

        log.info("Instagram Views API Response: {}", response.getBody());

        Map<String, Long> result = new HashMap<>();
        result.put("followers", 0L);
        result.put("nonFollowers", 0L);

        JsonNode data = response.getBody().get("data");
        if (data != null) {
            log.info("Instagram Views Data Size: {}", data.size());
            for (JsonNode insight : data) {
                // total_value 필드에서 값 가져오기
                JsonNode totalValue = insight.get("total_value");
                if (totalValue == null || !totalValue.has("value")) {
                    log.warn("No total_value found for views insight");
                    continue;
                }
                
                long value = totalValue.get("value").asLong();
                
                // breakdowns에서 followers/non-followers 구분
                if (totalValue.has("breakdowns") && totalValue.get("breakdowns").size() > 0) {
                    JsonNode breakdowns = totalValue.get("breakdowns").get(0);
                    if (breakdowns.has("results") && breakdowns.get("results").size() > 0) {
                        for (JsonNode breakdownResult : breakdowns.get("results")) {
                            if (breakdownResult.has("dimension_values") && breakdownResult.get("dimension_values").size() > 0) {
                                String followType = breakdownResult.get("dimension_values").get(0).asText();
                                long breakdownValue = breakdownResult.get("value").asLong();
                                
                                log.info("Views breakdown: {}, value: {}", followType, breakdownValue);
                                
                                if ("FOLLOWER".equals(followType)) {
                                    result.put("followers", breakdownValue);
                                } else if ("NON_FOLLOWER".equals(followType)) {
                                    result.put("nonFollowers", breakdownValue);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            log.warn("No data found in Instagram Views response");
        }

        log.info("Final Instagram Views result: {}", result);
        return result;
    }

    // 광고 데이터 조회
    private Long getAdsCount(String accessToken, String adAccountId, DateRange dateRange) {
        if (adAccountId == null) return 0L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String adsUrl = String.format(
                "https://graph.facebook.com/v22.0/%s/ads?fields=id,name,created_time",
                adAccountId);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                adsUrl, HttpMethod.GET, entity, JsonNode.class);

        JsonNode data = response.getBody().get("data");
        if (data == null) return 0L;

        LocalDate startDate = LocalDate.parse(dateRange.getStartDate());
        LocalDate endDate = LocalDate.parse(dateRange.getEndDate());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");

        long count = 0;
        for (JsonNode ad : data) {
            String createdTime = ad.get("created_time").asText();
            LocalDate adDate = LocalDate.parse(createdTime.substring(0, 10));
            
            if (!adDate.isBefore(startDate) && !adDate.isAfter(endDate)) {
                count++;
            }
        }

        return count;
    }

    // 업로드된 미디어 데이터 조회
    private Map<String, Long> getUploadData(String accessToken, String instagramBusinessAccountId, DateRange dateRange) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        String mediaUrl = String.format(
                "https://graph.facebook.com/v22.0/%s/media?fields=id,media_type,timestamp",
                instagramBusinessAccountId);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                mediaUrl, HttpMethod.GET, entity, JsonNode.class);

        JsonNode data = response.getBody().get("data");
        if (data == null) {
            Map<String, Long> result = new HashMap<>();
            result.put("total", 0L);
            return result;
        }

        LocalDate startDate = LocalDate.parse(dateRange.getStartDate());
        LocalDate endDate = LocalDate.parse(dateRange.getEndDate());

        long totalCount = 0;

        for (JsonNode media : data) {
            String timestamp = media.get("timestamp").asText();
            String mediaType = media.get("media_type").asText();
            LocalDate mediaDate = LocalDate.parse(timestamp.substring(0, 10));
            
            if (!mediaDate.isBefore(startDate) && !mediaDate.isAfter(endDate)) {
                totalCount++;
            }
        }

        Map<String, Long> result = new HashMap<>();
        result.put("total", totalCount);
        return result;
    }

    // 성장률 계산
    private Map<String, Double> calculateGrowthRates(List<MetaStatsDto> statsList) {
        Map<String, Double> growthRates = new HashMap<>();
        
        if (statsList.size() < 2) {
            growthRates.put("impressions", 0.0);
            growthRates.put("profileStats", 0.0);
            growthRates.put("followers", 0.0);
            growthRates.put("viewsFollowers", 0.0);
            growthRates.put("viewsNonFollowers", 0.0);
            growthRates.put("adsCount", 0.0);
            growthRates.put("uploadedMedia", 0.0);
            return growthRates;
        }

        MetaStatsDto current = statsList.get(0); // 최근 30일
        MetaStatsDto previous = statsList.get(1); // 30~60일

        growthRates.put("impressions", calculateGrowthRate(current.getImpressions(), previous.getImpressions()));
        growthRates.put("profileStats", calculateGrowthRate(current.getProfileStats(), previous.getProfileStats()));
        growthRates.put("followers", calculateGrowthRate(current.getFollowers(), previous.getFollowers()));
        growthRates.put("viewsFollowers", calculateGrowthRate(current.getViewsFollowers(), previous.getViewsFollowers()));
        growthRates.put("viewsNonFollowers", calculateGrowthRate(current.getViewsNonFollowers(), previous.getViewsNonFollowers()));
        growthRates.put("adsCount", calculateGrowthRate(current.getAdsCount(), previous.getAdsCount()));
        growthRates.put("uploadedMedia", calculateGrowthRate(current.getUploadedMedia(), previous.getUploadedMedia()));

        return growthRates;
    }

    private double calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        return ((double) (current - previous) / previous) * 100;
    }

    // 강점 분석 (성장률 상위 2개)
    private List<String> analyzeStrengths(List<MetaStatsDto> statsList) {
        Map<String, Double> growthRates = calculateGrowthRates(statsList);
        
        List<Map.Entry<String, Double>> sortedMetrics = growthRates.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 내림차순 정렬
                .toList();

        // 성장률 상위 2개를 강점으로 반환
        return List.of(sortedMetrics.get(0).getKey(), sortedMetrics.get(1).getKey());
    }

    // 약점 분석 (성장률 하위 2개)
    private List<String> analyzeWeaknesses(List<MetaStatsDto> statsList) {
        Map<String, Double> growthRates = calculateGrowthRates(statsList);
        
        List<Map.Entry<String, Double>> sortedMetrics = growthRates.entrySet()
                .stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 내림차순 정렬
                .toList();

        // 성장률 하위 2개를 약점으로 반환
        return List.of(sortedMetrics.get(sortedMetrics.size() - 1).getKey(), 
                      sortedMetrics.get(sortedMetrics.size() - 2).getKey());
    }
}
