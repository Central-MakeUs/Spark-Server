package com.example.spark.domain.flask.dto;

import com.example.spark.domain.meta.dto.MetaAnalysisResultDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetaDataCache {
    private final ConcurrentHashMap<String, MetaAnalysisResultDto> cache = new ConcurrentHashMap<>();

    // ✅ 데이터 저장 (MetaAnalysisResultDto를 저장)
    public void saveData(String instagramBusinessAccountId, MetaAnalysisResultDto data) {
        cache.put(instagramBusinessAccountId, data);
    }

    // ✅ 데이터 조회 후 삭제 (MetaAnalysisResultDto 반환)
    public MetaAnalysisResultDto getAndRemoveData(String instagramBusinessAccountId) {
        return cache.remove(instagramBusinessAccountId);
    }
} 