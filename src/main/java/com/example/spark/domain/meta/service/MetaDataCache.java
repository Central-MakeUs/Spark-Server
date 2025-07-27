package com.example.spark.domain.meta.service;

import com.example.spark.domain.meta.dto.MetaAnalysisResultDto;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetaDataCache {
    private final ConcurrentHashMap<String, MetaAnalysisResultDto> cache = new ConcurrentHashMap<>();

    // 데이터 저장 (MetaAnalysisResultDto를 저장)
    public void saveData(String instagramBusinessAccountId, MetaAnalysisResultDto data) {
        cache.put(instagramBusinessAccountId, data);
    }

    // 데이터 조회 (삭제하지 않음)
    public MetaAnalysisResultDto getData(String instagramBusinessAccountId) {
        return cache.get(instagramBusinessAccountId);
    }

    // 데이터 삭제
    public void removeData(String instagramBusinessAccountId) {
        cache.remove(instagramBusinessAccountId);
    }

    // 데이터 조회 후 삭제 (MetaAnalysisResultDto 반환) - 하위 호환성 유지
    public MetaAnalysisResultDto getAndRemoveData(String instagramBusinessAccountId) {
        return cache.remove(instagramBusinessAccountId);
    }
} 