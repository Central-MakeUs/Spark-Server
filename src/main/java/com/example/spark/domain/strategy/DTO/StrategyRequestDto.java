package com.example.spark.domain.strategy.DTO;

import lombok.Getter;

import java.util.List;

@Getter
public class StrategyRequestDto {
    private String activityDomain;
    private String workType;
    private String snsGoal;
    private List<String> weaknesses;
}
