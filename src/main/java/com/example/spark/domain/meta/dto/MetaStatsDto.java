package com.example.spark.domain.meta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaStatsDto {
    private String startDate;
    private String endDate;
    private Long impressions; // likes + comments + saves + shares
    private Long likes; // 개별 좋아요 수
    private Long comments; // 개별 댓글 수
    private Long profileStats; // profile_views + profile_links_taps
    private Long followers;
    private Long unfollowers;
    private Long viewsFollowers;
    private Long viewsNonFollowers;
    private Long adsCount;
    private Long uploadedMedia;
} 