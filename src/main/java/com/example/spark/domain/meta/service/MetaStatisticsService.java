package com.example.spark.domain.meta.service;

import com.example.spark.domain.meta.dto.MetaStatsDto;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class MetaStatisticsService {
    
    private static final String[] PERIODS = {"recent30Days", "days30to60", "days60to90"};
    
    public Map<String, Double> calculateMetaAverageViews(List<MetaStatsDto> stats) {
        validateStatsSize(stats, "평균 조회수 계산");
        
        return calculateAverage(stats, stat -> {
            double totalViews = stat.getViewsFollowers() + stat.getViewsNonFollowers();
            return (double) totalViews;
        });
    }

    public Map<String, Double> calculateMetaAverageLikes(List<MetaStatsDto> stats) {
        validateStatsSize(stats, "평균 좋아요수 계산");
        
        return calculateAverage(stats, stat -> {
            Long likes = stat.getLikes() != null ? stat.getLikes() : 0L;
            return (double) likes;
        });
    }

    public Map<String, Double> calculateMetaAverageComments(List<MetaStatsDto> stats) {
        validateStatsSize(stats, "평균 댓글수 계산");
        
        return calculateAverage(stats, stat -> {
            Long comments = stat.getComments() != null ? stat.getComments() : 0L;
            return (double) comments;
        });
    }
    
    // 검증 메서드
    private void validateStatsSize(List<MetaStatsDto> stats, String operation) {
        if (stats.size() < 3) {
            throw new RuntimeException(operation + "을 위해 최소 3개 기간 데이터가 필요합니다.");
        }
    }
    
    // 평균 계산 메서드
    private Map<String, Double> calculateAverage(List<MetaStatsDto> stats, Function<MetaStatsDto, Double> valueExtractor) {
        Map<String, Double> result = new LinkedHashMap<>();
        
        for (int i = 0; i < PERIODS.length; i++) {
            MetaStatsDto stat = stats.get(i);
            String period = PERIODS[i];
            
            double value = valueExtractor.apply(stat);
            int uploadedMedia = stat.getUploadedMedia() != null ? stat.getUploadedMedia().intValue() : 0;
            double average = uploadedMedia > 0 ? value / uploadedMedia : 0.0;
            
            result.put(period, Math.round(average * 100.0) / 100.0);
        }
        
        return result;
    }
} 