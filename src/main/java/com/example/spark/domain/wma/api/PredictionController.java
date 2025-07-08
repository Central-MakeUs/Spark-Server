package com.example.spark.domain.wma.api;

import com.example.spark.domain.flask.dto.YouTubeDataCache;
import com.example.spark.domain.wma.service.PredictionService;
import com.example.spark.domain.youtube.dto.YouTubeAnalysisResultDto;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Average Views API", description = "영상 1개당 평균 조회수를 계산하는 API")
@RestController
@RequiredArgsConstructor
public class PredictionController {
    private final PredictionService predictionService;
    private final YouTubeDataCache youTubeDataCache;
    
    @Operation(
            summary = "영상 1개당 평균 조회수 계산",
            description = """
                    최근 3개 기간의 데이터를 기반으로 각 기간별 영상 1개당 평균 조회수를 계산합니다.
                    
                    **계산 공식**
                    - 최근 30일: 최근 30일간 조회수 / 최근 30일간 업로드한 영상수
                    - 30~60일: 30~60일간 조회수 / 30~60일간 업로드한 영상수  
                    - 60~90일: 60~90일간 조회수 / 60~90일간 업로드한 영상수
                    
                    **요청값**
                    - `channelId`: 조회할 채널 ID
                    
                    **응답값**
                    - `recent30Days`: 최근 30일 평균 조회수
                    - `days30to60`: 30~60일 평균 조회수
                    - `days60to90`: 60~90일 평균 조회수
                    """
    )
    @GetMapping("/average-views-per-video")
    public SuccessResponse<Map<String, Double>> getAverageViewsPerVideo(@RequestParam String channelId) {
        YouTubeAnalysisResultDto analysisResult = null;
        int retryCount = 0;
        int maxRetries = 3; // 최대 3회 재시도
        int delayMillis = 700; // 0.7초 대기 후 재시도

        while (retryCount < maxRetries) {
            analysisResult = youTubeDataCache.getAndRemoveData(channelId);

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

        // 영상 1개당 평균 조회수 계산 수행
        Map<String, Double> averageViews = predictionService.calculateAverageViewsPerVideo(analysisResult.getStats());

        return SuccessResponse.success(averageViews);
    }
}
