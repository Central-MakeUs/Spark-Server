package com.example.spark.domain.wma.service;

import com.example.spark.domain.youtube.dto.YouTubeCombinedStatsDto;
import com.example.spark.domain.meta.dto.MetaStatsDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class PredictionService {
    
    // YouTube 영상 1개당 평균 조회수 계산
    public Map<String, Double> calculateAverageViewsPerVideo(List<YouTubeCombinedStatsDto> stats) {
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

    // Instagram 미디어 1개당 평균 조회수 계산
    public Map<String, Double> calculateMetaAverageViewsPerVideo(List<MetaStatsDto> stats) {
        if (stats.size() < 3) {
            throw new RuntimeException("평균 조회수 계산을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        Map<String, Double> averageViews = new LinkedHashMap<>(); // 순서 보장

        // recent30Days
        MetaStatsDto stat0 = stats.get(0);
        double totalViews0 = stat0.getViewsFollowers() + stat0.getViewsNonFollowers();
        int uploadedMedia0 = stat0.getUploadedMedia().intValue();
        double avgViews0 = uploadedMedia0 > 0 ? totalViews0 / uploadedMedia0 : 0.0;
        averageViews.put("recent30Days", Math.round(avgViews0 * 100.0) / 100.0);

        // days30to60
        MetaStatsDto stat1 = stats.get(1);
        double totalViews1 = stat1.getViewsFollowers() + stat1.getViewsNonFollowers();
        int uploadedMedia1 = stat1.getUploadedMedia().intValue();
        double avgViews1 = uploadedMedia1 > 0 ? totalViews1 / uploadedMedia1 : 0.0;
        averageViews.put("days30to60", Math.round(avgViews1 * 100.0) / 100.0);

        // days60to90
        MetaStatsDto stat2 = stats.get(2);
        double totalViews2 = stat2.getViewsFollowers() + stat2.getViewsNonFollowers();
        int uploadedMedia2 = stat2.getUploadedMedia().intValue();
        double avgViews2 = uploadedMedia2 > 0 ? totalViews2 / uploadedMedia2 : 0.0;
        averageViews.put("days60to90", Math.round(avgViews2 * 100.0) / 100.0);

        return averageViews;
    }

    // 기간별 키 생성
    private String getPeriodKey(int index) {
        switch (index) {
            case 0:
                return "recent30Days"; // 최근 30일
            case 1:
                return "days30to60";   // 30~60일
            case 2:
                return "days60to90";   // 60~90일
            default:
                return "period" + (index + 1);
        }
    }
}
