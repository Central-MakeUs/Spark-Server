package com.example.spark.domain.strategy.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OpenAIEmbeddingResponse {

    @JsonProperty("data")
    private List<EmbeddingData> data;

    @Getter
    @NoArgsConstructor
    public static class EmbeddingData {
        @JsonProperty("embedding")
        private List<Float> embedding;
    }
}
