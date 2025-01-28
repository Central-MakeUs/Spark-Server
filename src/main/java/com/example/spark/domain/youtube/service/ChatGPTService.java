package com.example.spark.domain.youtube.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatGPTService {

    private final RestTemplate restTemplate;

    public ChatGPTService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateStrategy(String predictionData) {
        String chatGPTEndpoint = "https://api.openai.com/v1/completions";
        String prompt = "Based on the prediction data: " + predictionData + " suggest a growth strategy.";

        // OpenAI API 요청 구성
        // Authorization 헤더 및 요청 Body 추가

        return restTemplate.postForObject(chatGPTEndpoint, prompt, String.class);
    }
}