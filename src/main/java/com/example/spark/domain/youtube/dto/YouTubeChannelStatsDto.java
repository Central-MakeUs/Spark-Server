package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeChannelStatsDto {
    private int views;
    private int subscribersGained;
    private int subscribersLost;
}
