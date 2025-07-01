package com.example.spark.domain.wma.service;

import com.example.spark.domain.youtube.dto.YouTubeCombinedStatsDto;
import com.example.spark.domain.meta.dto.MetaStatsDto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class PredictionService {
    public Map<String, Double> calculateWMAPredictions(List<YouTubeCombinedStatsDto> stats) {
        if (stats.size() < 3) {
            throw new RuntimeException("WMA 예측을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        // 가중치 설정 (최근 데이터에 더 높은 가중치)
        double w1 = 0.5;
        double w2 = 0.3;
        double w3 = 0.2;

        // 최근 3개 기간 데이터
        double recentViews = stats.get(0).getViews();
        double midViews = stats.get(1).getViews();
        double oldViews = stats.get(2).getViews();

        double recentSubscribers = stats.get(0).getNetSubscribers();
        double midSubscribers = stats.get(1).getNetSubscribers();
        double oldSubscribers = stats.get(2).getNetSubscribers();

        // WMA 계산
        double predictedViews = (w1 * recentViews) + (w2 * midViews) + (w3 * oldViews);
        double predictedNetSubscribers = (w1 * recentSubscribers) + (w2 * midSubscribers) + (w3 * oldSubscribers);

        // 결과 반환
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("predictedViews", Double.valueOf((int) Math.round(predictedViews)));
        predictions.put("predictedNetSubscribers", Double.valueOf((int) Math.round(predictedNetSubscribers)));

        return predictions;
    }

    public Map<String, Double> calculateMetaWMAPredictions(List<MetaStatsDto> stats) {
        if (stats.size() < 3) {
            throw new RuntimeException("WMA 예측을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        // 가중치 설정 (최근 데이터에 더 높은 가중치)
        double w1 = 0.5;
        double w2 = 0.3;
        double w3 = 0.2;

        // 최근 3개 기간 데이터 - 팔로워수
        double recentFollowers = stats.get(0).getFollowers();
        double midFollowers = stats.get(1).getFollowers();
        double oldFollowers = stats.get(2).getFollowers();

        // 최근 3개 기간 데이터 - 조회수 (follower + non-follower 합계)
        double recentViews = stats.get(0).getViewsFollowers() + stats.get(0).getViewsNonFollowers();
        double midViews = stats.get(1).getViewsFollowers() + stats.get(1).getViewsNonFollowers();
        double oldViews = stats.get(2).getViewsFollowers() + stats.get(2).getViewsNonFollowers();

        // WMA 계산
        double predictedFollowers = (w1 * recentFollowers) + (w2 * midFollowers) + (w3 * oldFollowers);
        double predictedViews = (w1 * recentViews) + (w2 * midViews) + (w3 * oldViews);

        // 결과 반환
        Map<String, Double> predictions = new HashMap<>();
        predictions.put("predictedFollowers", Double.valueOf((int) Math.round(predictedFollowers)));
        predictions.put("predictedViews", Double.valueOf((int) Math.round(predictedViews)));

        return predictions;
    }
}
