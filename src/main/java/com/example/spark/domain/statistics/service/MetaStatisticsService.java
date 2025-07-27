package com.example.spark.domain.statistics.service;

import com.example.spark.domain.statistics.dto.MetaStatsDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetaStatisticsService {
    public Map<String, Double> calculateMetaAverageViews(List<MetaStatsDto> stats) {
        if (stats.size() < 3) {
            throw new RuntimeException("평균 조회수 계산을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        Map<String, Double> averageViews = new LinkedHashMap<>(); // 순서 보장

        // recent30Days
        MetaStatsDto stat0 = stats.get(0);
        double totalViews0 = stat0.getViewsFollowers() + stat0.getViewsNonFollowers();
        int uploadedMedia0 = stat0.getUploadedMedia() != null ? stat0.getUploadedMedia().intValue() : 0;
        double avgViews0 = uploadedMedia0 > 0 ? totalViews0 / uploadedMedia0 : 0.0;
        averageViews.put("recent30Days", Math.round(avgViews0 * 100.0) / 100.0);

        // days30to60
        MetaStatsDto stat1 = stats.get(1);
        double totalViews1 = stat1.getViewsFollowers() + stat1.getViewsNonFollowers();
        int uploadedMedia1 = stat1.getUploadedMedia() != null ? stat1.getUploadedMedia().intValue() : 0;
        double avgViews1 = uploadedMedia1 > 0 ? totalViews1 / uploadedMedia1 : 0.0;
        averageViews.put("days30to60", Math.round(avgViews1 * 100.0) / 100.0);

        // days60to90
        MetaStatsDto stat2 = stats.get(2);
        double totalViews2 = stat2.getViewsFollowers() + stat2.getViewsNonFollowers();
        int uploadedMedia2 = stat2.getUploadedMedia() != null ? stat2.getUploadedMedia().intValue() : 0;
        double avgViews2 = uploadedMedia2 > 0 ? totalViews2 / uploadedMedia2 : 0.0;
        averageViews.put("days60to90", Math.round(avgViews2 * 100.0) / 100.0);

        return averageViews;
    }
} 