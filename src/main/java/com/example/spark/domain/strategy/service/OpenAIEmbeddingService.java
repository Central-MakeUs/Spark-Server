package com.example.spark.domain.strategy.service;

import com.example.spark.domain.strategy.DTO.OpenAIEmbeddingResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAIEmbeddingService {
    private WebClient webClient;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    public OpenAIEmbeddingService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/embeddings")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @PostConstruct
    public void init() {
        if (this.openAiApiKey == null || this.openAiApiKey.isBlank()) {
            throw new IllegalStateException("🚨 OpenAI API Key가 설정되지 않았습니다. application.yml을 확인하세요.");
        }

        System.out.println("📢 OpenAI API Key 로드 성공: " + this.openAiApiKey);

        // 🔹 API 키를 포함한 WebClient 재설정
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/embeddings")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + this.openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<Float> getEmbedding(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("🚨 입력 텍스트가 비어 있습니다.");
        }

        Map<String, Object> requestBody = Map.of(
                "input", List.of(text),
                "model", "text-embedding-3-small"
        );

        try {
            System.out.println("📤 OpenAI 요청: " + requestBody);

            OpenAIEmbeddingResponse response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(OpenAIEmbeddingResponse.class)
                    .block();

            if (response == null || response.getData().isEmpty()) {
                throw new RuntimeException("🚨 OpenAI 응답이 비어 있습니다.");
            }
            //System.out.println("UserInput embedding: " + response.getData().get(0).getEmbedding());
            return response.getData().get(0).getEmbedding();
        } catch (Exception e) {
            System.err.println("🚨 OpenAI Embedding 요청 실패: " + e.getMessage());
            throw new RuntimeException("OpenAI Embedding 요청 실패", e);
        }
    }
}
