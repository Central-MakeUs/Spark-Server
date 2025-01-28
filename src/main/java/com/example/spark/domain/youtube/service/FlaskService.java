package com.example.spark.domain.youtube.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FlaskService {

    private final RestTemplate restTemplate;

    public FlaskService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getGrowthPrediction(String jsonData) {
        String flaskEndpoint = "http://localhost:5000/predict";
        return restTemplate.postForObject(flaskEndpoint, jsonData, String.class);
    }
}