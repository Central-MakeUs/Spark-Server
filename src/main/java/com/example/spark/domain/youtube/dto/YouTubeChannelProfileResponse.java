package com.example.spark.domain.youtube.dto;

import java.util.List;

public class YouTubeChannelProfileResponse {
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public static class Item {
        private String id;
        private Snippet snippet;
        private Statistics statistics;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Snippet getSnippet() {
            return snippet;
        }

        public void setSnippet(Snippet snippet) {
            this.snippet = snippet;
        }

        public Statistics getStatistics() {
            return statistics;
        }

        public void setStatistics(Statistics statistics) {
            this.statistics = statistics;
        }

        public static class Snippet {
            private String title;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }
        }

        public static class Statistics {
            private Long viewCount;
            private Long subscriberCount;
            private Long videoCount;

            public Long getViewCount() {
                return viewCount;
            }

            public void setViewCount(Long viewCount) {
                this.viewCount = viewCount;
            }

            public Long getSubscriberCount() {
                return subscriberCount;
            }

            public void setSubscriberCount(Long subscriberCount) {
                this.subscriberCount = subscriberCount;
            }

            public Long getVideoCount() {
                return videoCount;
            }

            public void setVideoCount(Long videoCount) {
                this.videoCount = videoCount;
            }
        }
    }
}
