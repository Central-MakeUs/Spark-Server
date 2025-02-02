package com.example.spark.domain.flask.dto;

import com.example.spark.domain.youtube.dto.YouTubeCombinedStatsDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class YouTubeDataCache {
    private final ConcurrentHashMap<String, List<YouTubeCombinedStatsDto>> cache = new ConcurrentHashMap<>();

    // 데이터 저장 (List 타입 유지)
    public void saveData(String channelId, List<YouTubeCombinedStatsDto> data) {
        cache.put(channelId, data);
    }

    // 데이터 조회 후 삭제 (List 반환)
    public List<YouTubeCombinedStatsDto> getAndRemoveData(String channelId) {
        return cache.remove(channelId);
    }
}
