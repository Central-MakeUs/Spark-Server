package com.example.spark.domain.youtube.dto;

public class YouTubeChannelProfileDto {
    private String channelId;
    private String channelName;
    private Long totalVideoCount;
    private Long subscriberCount;
    private Long totalViewCount;

    public YouTubeChannelProfileDto(String channelId, String channelName, Long totalVideoCount, Long subscriberCount, Long totalViewCount) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.totalVideoCount = totalVideoCount;
        this.subscriberCount = subscriberCount;
        this.totalViewCount = totalViewCount;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public Long getTotalVideoCount() {
        return totalVideoCount;
    }

    public Long getSubscriberCount() {
        return subscriberCount;
    }

    public Long getTotalViewCount() {
        return totalViewCount;
    }
}
