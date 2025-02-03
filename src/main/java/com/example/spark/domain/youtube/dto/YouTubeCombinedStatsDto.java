package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class YouTubeCombinedStatsDto {
    private final String startDate;
    private final String endDate;
    private final long views;
    private final long netSubscribers; // ✅ (subscribersGained - subscribersLost) 자동 계산
    private final long likes;
    private final long comments;
    private final long shares;
    private final double estimatedRevenue;
    private final long averageViewDuration;
    private final int uploadedVideos;

    // ✅ Builder 내부에서 netSubscribers 자동 계산
    public static YouTubeCombinedStatsDto of(String startDate, String endDate, long views,
                                             long subscribersGained, long subscribersLost, long likes,
                                             long comments, long shares, double estimatedRevenue,
                                             long averageViewDuration, int uploadedVideos) {
        return YouTubeCombinedStatsDto.builder()
                .startDate(startDate)
                .endDate(endDate)
                .views(views)
                .netSubscribers(subscribersGained - subscribersLost) // 자동 계산
                .likes(likes)
                .comments(comments)
                .shares(shares)
                .estimatedRevenue(estimatedRevenue)
                .averageViewDuration(averageViewDuration)
                .uploadedVideos(uploadedVideos)
                .build();
    }
}
