package com.example.spark.domain.meta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaProfileDto {
    private String userName;
    private String profileUrl;
    private Long followersCount;
    private Long followingCount;
    private Long postsCount;
    private String instagramBusinessAccountId;
}