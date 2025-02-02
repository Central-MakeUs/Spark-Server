package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouTubeCombinedStatsDto {
    private final String startDate;
    private final String endDate;
    private final long views;
    private final long subscribersGained;
    private final long subscribersLost;
    private final long likes;
    private final long comments;
    private final long shares;
    private final double estimatedRevenue;
    private final long averageViewDuration;
    private final int uploadedVideos;
}
