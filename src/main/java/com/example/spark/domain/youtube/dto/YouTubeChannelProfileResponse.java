package com.example.spark.domain.youtube.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeChannelProfileResponse {
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String id;
        private Snippet snippet;
        private Statistics statistics;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Snippet {
        private String title;
        private Thumbnails thumbnails;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Thumbnails {
        @JsonProperty("default")
        private ThumbnailDetail defaultThumbnail;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThumbnailDetail {
        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long viewCount;
        private Long subscriberCount;
        private Long videoCount;
    }
}
