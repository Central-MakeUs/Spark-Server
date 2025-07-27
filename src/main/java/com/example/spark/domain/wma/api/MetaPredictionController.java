package com.example.spark.domain.wma.api;

import com.example.spark.domain.flask.dto.MetaDataCache;
import com.example.spark.domain.wma.service.PredictionService;
import com.example.spark.domain.meta.dto.MetaAnalysisResultDto;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Meta Prediction API", description = "3개월 뒤 Instagram 지표를 계산하는 API")
@RestController
@RequiredArgsConstructor
public class MetaPredictionController {
    private final PredictionService predictionService;
    private final MetaDataCache metaDataCache;
    
    @Operation(
            summary = "WMA 기반 Instagram 성장 예측",
            description = """
                    최근 3개 기간의 Instagram 지표 데이터를 기반으로
                    3개월 뒤 주요 지표들을 가중 이동 평균(WMA)으로 예측합니다.
                    
                    **요청값**
                    - `instagramBusinessAccountId`: 조회할 Instagram 비즈니스 계정 ID
                    
                    **응답값**
                    - 3개월 뒤 팔로워수 예측
                    - 3개월 뒤 조회수 예측 (팔로워 + 비팔로워 합계)
                    """
    )
    @GetMapping("/meta-predictions")
    public SuccessResponse<Map<String, Double>> getMetaWmaPredictions(@RequestParam String instagramBusinessAccountId) {

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
            throw new RuntimeException("WMA 예측을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        // WMA 기반 성장 예측 수행
        Map<String, Double> wmaPredictions = predictionService.calculateMetaWMAPredictions(analysisResult.getStats());

        return SuccessResponse.success(wmaPredictions);
    }
} 