package com.example.spark.domain.strategy.DTO;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StrategyCache {
    // 요청 ID별 ChatGPT 전략 저장 (비동기 결과)
    private final Map<String, CompletableFuture<String>> cache = new ConcurrentHashMap<>();

    // ✅ 전략 저장
    public void put(String requestId, CompletableFuture<String> futureStrategy) {
        cache.put(requestId, futureStrategy);
    }

    // ✅ 전략 가져오기
    public CompletableFuture<String> get(String requestId) {
        return cache.get(requestId);
    }

    // ✅ 전략 삭제 (완료된 요청 정리)
    public void remove(String requestId) {
        cache.remove(requestId);
    }
}
