package com.example.spark.domain.youtube.api;

import com.example.spark.domain.youtube.service.YouTubeDataCache;
import com.example.spark.domain.youtube.dto.YouTubeAnalysisResultDto;
import com.example.spark.domain.youtube.service.YouTubeStatisticsService;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "YouTube(Google) - Statistics", description = "YouTube 영상 1개당 평균 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/youtube")
public class YouTubeStatisticsController {
    private final YouTubeStatisticsService youTubeStatisticsService;
    private final YouTubeDataCache youTubeDataCache;

    @Operation(
            summary = "YouTube 영상 1개당 평균 성과 지표",
            description = """
                    최근 3개 기간의 데이터를 기반으로 각 기간별 영상 1개당 평균 조회수, 좋아요수, 댓글수를 계산합니다.
                    
                    **계산 공식**
                    - 최근 30일: 최근 30일간 지표 / 최근 30일간 업로드한 영상수
                    - 30~60일: 30~60일간 지표 / 30~60일간 업로드한 영상수
                    - 60~90일: 60~90일간 지표 / 60~90일간 업로드한 영상수
                    
                    **요청값**
                    - `channelId`: 조회할 YouTube 채널 ID
                    
                    **응답값**
                    - `averageViews`: 기간별 평균 조회수
                    - `averageLikes`: 기간별 평균 좋아요수
                    - `averageComments`: 기간별 평균 댓글수
                    """
    )
    @GetMapping("/statistics/performance")
    public SuccessResponse<Map<String, Object>> getYouTubePerformance(@RequestParam String channelId) {
        YouTubeAnalysisResultDto analysisResult = getAnalysisResult(channelId);
        
        Map<String, Object> performance = new HashMap<>();
        performance.put("averageViews", youTubeStatisticsService.calculateYouTubeAverageViews(analysisResult.getStats()));
        performance.put("averageLikes", youTubeStatisticsService.calculateYouTubeAverageLikes(analysisResult.getStats()));
        performance.put("averageComments", youTubeStatisticsService.calculateYouTubeAverageComments(analysisResult.getStats()));
        
        return SuccessResponse.success(performance);
    }
    
    // 공통 데이터 조회 메서드
    private YouTubeAnalysisResultDto getAnalysisResult(String channelId) {
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
            throw new RuntimeException("평균 통계 계산을 위해 최소 3개 기간 데이터가 필요합니다.");
        }
        
        return analysisResult;
    }
} 