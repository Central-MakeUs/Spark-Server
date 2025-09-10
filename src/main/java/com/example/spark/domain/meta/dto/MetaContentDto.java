package com.example.spark.domain.meta.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetaContentDto {
    private String id;
    private String caption;
    private String timestamp;
    private String mediaType;
    private String contentUrl;
    private Long views;
    private Long likes;
} 