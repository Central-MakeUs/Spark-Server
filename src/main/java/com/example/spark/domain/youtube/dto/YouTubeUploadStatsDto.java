package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class YouTubeUploadStatsDto {
    private final String startDate;
    private final String endDate;
    private final int uploadedVideos;
}
