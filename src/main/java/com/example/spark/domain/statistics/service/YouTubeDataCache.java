package com.example.spark.domain.statistics.service;

import com.example.spark.domain.statistics.dto.YouTubeAnalysisResultDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class YouTubeDataCache {
    private final ConcurrentHashMap<String, YouTubeAnalysisResultDto> cache = new ConcurrentHashMap<>();

    // 데이터 저장 (YouTubeAnalysisResultDto를 저장)
    public void saveData(String channelId, YouTubeAnalysisResultDto data) {
        cache.put(channelId, data);
    }

    // 데이터 조회 (삭제하지 않음)
    public YouTubeAnalysisResultDto getData(String channelId) {
        return cache.get(channelId);
    }

    // 데이터 삭제
    public void removeData(String channelId) {
        cache.remove(channelId);
    }

    // 데이터 조회 후 삭제 (YouTubeAnalysisResultDto 반환) - 하위 호환성 유지
    public YouTubeAnalysisResultDto getAndRemoveData(String channelId) {
        return cache.remove(channelId);
    }
} 