package com.example.spark.domain.youtube.service;

import com.example.spark.domain.youtube.dto.YouTubeCombinedStatsDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class YouTubeStatisticsService {
    
    private static final String[] PERIODS = {"recent30Days", "days30to60", "days60to90"};
    
    public Map<String, Double> calculateYouTubeAverageViews(List<YouTubeCombinedStatsDto> stats) {
        validateStatsSize(stats, "평균 조회수 계산");
        
        return calculateAverage(stats, stat -> (double) stat.getViews());
    }

    public Map<String, Double> calculateYouTubeAverageLikes(List<YouTubeCombinedStatsDto> stats) {
        validateStatsSize(stats, "평균 좋아요수 계산");
        
        return calculateAverage(stats, stat -> (double) stat.getLikes());
    }

    public Map<String, Double> calculateYouTubeAverageComments(List<YouTubeCombinedStatsDto> stats) {
        validateStatsSize(stats, "평균 댓글수 계산");
        
        return calculateAverage(stats, stat -> (double) stat.getComments());
    }
    
    // 검증 메서드
    private void validateStatsSize(List<YouTubeCombinedStatsDto> stats, String operation) {
        if (stats.size() < 3) {
            throw new RuntimeException(operation + "을 위해 최소 3개 기간 데이터가 필요합니다.");
        }
    }
    
    // 평균 계산 메서드
    private Map<String, Double> calculateAverage(List<YouTubeCombinedStatsDto> stats, Function<YouTubeCombinedStatsDto, Double> valueExtractor) {
        Map<String, Double> averages = new LinkedHashMap<>(); // 순서 보장

        for (int i = 0; i < PERIODS.length; i++) {
            YouTubeCombinedStatsDto stat = stats.get(i);
            double totalValue = valueExtractor.apply(stat);
            int uploadedVideos = stat.getUploadedVideos();
            double average = uploadedVideos > 0 ? totalValue / uploadedVideos : 0.0;
            averages.put(PERIODS[i], Math.round(average * 100.0) / 100.0);
        }

        return averages;
    }
} 