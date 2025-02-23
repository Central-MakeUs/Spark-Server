package com.example.spark.domain.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class YouTubeAnalyticsResponse {

    private List<ColumnHeader> columnHeaders;
    private List<List<String>> rows;

    @Data
    public static class ColumnHeader {
        private String name;
        private String columnType;
        private String dataType;
    }
}
