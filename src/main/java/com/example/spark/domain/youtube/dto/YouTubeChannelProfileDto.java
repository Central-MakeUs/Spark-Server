package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YouTubeChannelProfileDto {
    private String channelId;
    private String channelName;
    private Long totalVideoCount;
    private Long subscriberCount;
    private Long totalViewCount;
    private String defaultThumbnailUrl;
}