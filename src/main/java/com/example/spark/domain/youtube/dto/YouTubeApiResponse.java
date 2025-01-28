package com.example.spark.domain.youtube.dto;

import java.util.List;

public class YouTubeApiResponse {
    private List<YouTubeVideoDto> items;

    public List<YouTubeVideoDto> getItems() {
        return items;
    }

    public void setItems(List<YouTubeVideoDto> items) {
        this.items = items;
    }
}
