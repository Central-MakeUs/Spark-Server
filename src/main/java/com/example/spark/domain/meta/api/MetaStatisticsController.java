package com.example.spark.domain.meta.api;

import com.example.spark.domain.meta.service.MetaDataCache;
import com.example.spark.domain.meta.dto.MetaAnalysisResultDto;
import com.example.spark.domain.meta.service.MetaStatisticsService;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Meta - Statistics", description = "Instagram 미디어 1개당 평균 조회수 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/meta")
public class MetaStatisticsController {
    private final MetaStatisticsService metaStatisticsService;
    private final MetaDataCache metaDataCache;

    @Operation(
            summary = "Instagram 미디어 1개당 평균 조회수 계산",
            description = """
                    최근 3개 기간의 데이터를 기반으로 각 기간별 미디어 1개당 평균 조회수를 계산합니다.
                    
                    **계산 공식**
                    - 최근 30일: 최근 30일간 조회수 / 최근 30일간 업로드한 미디어수
                    - 30~60일: 30~60일간 조회수 / 30~60일간 업로드한 미디어수  
                    - 60~90일: 60~90일간 조회수 / 60~90일간 업로드한 미디어수
                    
                    **요청값**
                    - `instagramBusinessAccountId`: 조회할 Instagram 비즈니스 계정 ID
                    
                    **응답값**
                    - `recent30Days`: 최근 30일 평균 조회수
                    - `days30to60`: 30~60일 평균 조회수
                    - `days60to90`: 60~90일 평균 조회수
                    """
    )
    @GetMapping("/statistics/average-views")
    public SuccessResponse<Map<String, Double>> getMetaAverageViews(@RequestParam String instagramBusinessAccountId) {
        MetaAnalysisResultDto analysisResult = null;
        int retryCount = 0;
        int maxRetries = 3; // 최대 3회 재시도
        int delayMillis = 700; // 0.7초 대기 후 재시도

        while (retryCount < maxRetries) {
            analysisResult = metaDataCache.getAndRemoveData(instagramBusinessAccountId);

            if (analysisResult != null && analysisResult.getStats().size() >= 3) {
                break; // 데이터가 충분하면 반복문 탈출
            }

            retryCount++;

            try {
                Thread.sleep(delayMillis); // 0.7초 대기 후 재시도
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("재시도 중 인터럽트 발생", e);
            }
        }

        // 최종적으로도 데이터 부족하면 예외 발생
        if (analysisResult == null || analysisResult.getStats().size() < 3) {
            throw new RuntimeException("평균 조회수 계산을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        // 미디어 1개당 평균 조회수 계산 수행
        Map<String, Double> averageViews = metaStatisticsService.calculateMetaAverageViews(analysisResult.getStats());

        return SuccessResponse.success(averageViews);
    }
} 