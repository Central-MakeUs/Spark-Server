package com.example.spark.domain.meta.dto;

import com.example.spark.domain.meta.dto.MetaStatsDto;
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
public class MetaAnalysisResultDto {
    private List<MetaStatsDto> stats;
    private Map<String, Double> growthRates;
    private List<String> strengths;
    private List<String> weaknesses;
} 