package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class YouTubeAnalysisResultDto {
    private final List<YouTubeCombinedStatsDto> stats; // 3개 기간의 기존 통계 데이터
    private final Map<String, Double> growthRates; // 성장률 분석 데이터 (각 지표별 % 증가율)
    private final List<String> strengths; // 강점 (상위 2개 지표)
    private final List<String> weaknesses; // 약점 (하위 2개 지표)
}
