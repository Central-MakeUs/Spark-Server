package com.example.spark.domain.youtube.dto;

import java.util.List;

public class YouTubeAnalyticsResponse {
    private List<List<String>> rows;

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }
}

