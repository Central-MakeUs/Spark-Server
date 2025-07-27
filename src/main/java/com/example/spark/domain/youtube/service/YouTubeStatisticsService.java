package com.example.spark.domain.youtube.service;

import com.example.spark.domain.youtube.dto.YouTubeCombinedStatsDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class YouTubeStatisticsService {
    public Map<String, Double> calculateYouTubeAverageViews(List<YouTubeCombinedStatsDto> stats) {
        if (stats.size() < 3) {
            throw new RuntimeException("평균 조회수 계산을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        Map<String, Double> averageViews = new LinkedHashMap<>(); // 순서 보장

        // recent30Days
        YouTubeCombinedStatsDto stat0 = stats.get(0);
        double totalViews0 = stat0.getViews();
        int uploadedVideos0 = stat0.getUploadedVideos();
        double avgViews0 = uploadedVideos0 > 0 ? totalViews0 / uploadedVideos0 : 0.0;
        averageViews.put("recent30Days", Math.round(avgViews0 * 100.0) / 100.0);

        // days30to60
        YouTubeCombinedStatsDto stat1 = stats.get(1);
        double totalViews1 = stat1.getViews();
        int uploadedVideos1 = stat1.getUploadedVideos();
        double avgViews1 = uploadedVideos1 > 0 ? totalViews1 / uploadedVideos1 : 0.0;
        averageViews.put("days30to60", Math.round(avgViews1 * 100.0) / 100.0);

        // days60to90
        YouTubeCombinedStatsDto stat2 = stats.get(2);
        double totalViews2 = stat2.getViews();
        int uploadedVideos2 = stat2.getUploadedVideos();
        double avgViews2 = uploadedVideos2 > 0 ? totalViews2 / uploadedVideos2 : 0.0;
        averageViews.put("days60to90", Math.round(avgViews2 * 100.0) / 100.0);

        return averageViews;
    }
} 