package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeVideoDto {
    private String id;
    private Snippet snippet;
    private Statistics statistics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Snippet {
        private String title;
        private String publishedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long viewCount;
    }
}
