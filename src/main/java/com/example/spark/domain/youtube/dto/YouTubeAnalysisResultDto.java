package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeAnalysisResultDto {
    private List<YouTubeCombinedStatsDto> stats;
    private Map<String, Double> growthRates;
    private List<String> strengths;
    private List<String> weaknesses;
} 