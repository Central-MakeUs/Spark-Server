package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YouTubeChannelStatsDto {
    private String startDate;          // 통계 시작 날짜 (YYYY-MM-DD 형식)
    private String endDate;            // 통계 종료 날짜 (YYYY-MM-DD 형식)
    private long views;                // 조회수
    private long subscribersGained;    // 증가한 구독자 수
    private long subscribersLost;      // 감소한 구독자 수
    private long likes;                // 좋아요 수
    private long comments;             // 댓글 수
    private long shares;               // 공유 수
    private Double estimatedRevenue;     // 예상 수익 (단위: USD 또는 지정된 통화)
    private long averageViewDuration;  // 평균 시청 시간 (초)
}
