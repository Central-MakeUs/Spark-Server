package com.example.spark.domain.strategy.DTO;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class PineconeResponseDto {
    private List<Match> matches;

    @Data
    public static class Match {
        private String id;
        private float score;
        private Map<String, Object> metadata;
    }
}