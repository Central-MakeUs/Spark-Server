package com.example.spark.global.cache;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StrategyCache {
    private final Map<String, CompletableFuture<Map<String, ?>>> cache = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<Map<String, ?>> futureStrategy) {
        cache.put(requestId, futureStrategy);
    }

    public CompletableFuture<Map<String, ?>> get(String requestId) {
        return cache.get(requestId);
    }

    public void remove(String requestId) {
        cache.remove(requestId);
    }
}
