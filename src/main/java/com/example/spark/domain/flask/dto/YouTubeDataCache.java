package com.example.spark.domain.flask.dto;

import com.example.spark.domain.youtube.dto.YouTubeAnalysisResultDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class YouTubeDataCache {
    private final ConcurrentHashMap<String, YouTubeAnalysisResultDto> cache = new ConcurrentHashMap<>();

    // ✅ 데이터 저장 (YouTubeAnalysisResultDto를 저장)
    public void saveData(String channelId, YouTubeAnalysisResultDto data) {
        cache.put(channelId, data);
    }

    // ✅ 데이터 조회 후 삭제 (YouTubeAnalysisResultDto 반환)
    public YouTubeAnalysisResultDto getAndRemoveData(String channelId) {
        return cache.remove(channelId);
    }
}
