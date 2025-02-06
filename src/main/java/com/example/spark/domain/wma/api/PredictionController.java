package com.example.spark.domain.wma.api;


import com.example.spark.domain.youtube.dto.YouTubeAnalysisResultDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;
import com.example.spark.global.response.SuccessResponse;
import org.springframework.web.bind.annotation.RestController;
import com.example.spark.domain.wma.service.PredictionService;
import com.example.spark.domain.flask.dto.YouTubeDataCache;
@Tag(name = "Prediction API", description = "3개월 뒤 조회수/구독자수를 계산하는 API")
@RestController
@RequiredArgsConstructor
public class PredictionController {
    private final PredictionService PredictionService;
    private final YouTubeDataCache YouTubeDataCache;
    
    @Operation(
            summary = "WMA 기반 성장 예측",
            description = """
                    최근 3개 기간의 조회수/구독자수 데이터를 기반으로
                    3개월 뒤 조회수/구독자수를 가중 이동 평균(WMA)으로 예측합니다.
                    
                    **요청값**
                    - `channelId`: 조회할 채널 ID
                    
                    **응답값**
                    - 3개월 뒤 조회수 예측
                    - 3개월 뒤 구독자수 예측
                    """
    )
    @GetMapping("/channel-predictions")
    public SuccessResponse<Map<String, Double>> getWmaPredictions(
            @RequestParam String channelId) {

        // 캐시에서 데이터 조회 후 즉시 삭제
        YouTubeAnalysisResultDto analysisResult = YouTubeDataCache.getAndRemoveData(channelId);

        if (analysisResult == null || analysisResult.getStats().size() < 3) {
            throw new RuntimeException("WMA 예측을 위해 최소 3개 기간 데이터가 필요합니다.");
        }

        // WMA 기반 성장 예측 수행
        Map<String, Double> wmaPredictions = PredictionService.calculateWMAPredictions(analysisResult.getStats());

        return SuccessResponse.success(wmaPredictions);
    }




}
