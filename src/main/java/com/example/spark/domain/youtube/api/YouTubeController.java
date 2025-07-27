package com.example.spark.domain.youtube.api;

import com.example.spark.domain.youtube.service.YouTubeDataCache;
import com.example.spark.domain.youtube.dto.YouTubeAnalysisResultDto;
import com.example.spark.domain.youtube.dto.YouTubeChannelProfileDto;
import com.example.spark.domain.youtube.dto.YouTubeVideoDto;
import com.example.spark.domain.youtube.service.YouTubeService;
import com.example.spark.global.error.ErrorCode;
import com.example.spark.global.response.SuccessResponse;
import com.example.spark.global.error.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "YouTube(Google) - Analytics", description = "YouTube Analytics 데이터를 관리하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;
    private final YouTubeDataCache youTubeDataCache;


    @Operation(
            summary = "채널 정보 조회",
            description = """
                    연동된 채널의 프로필을 조회합니다.
                                          
                    **요청값**
                    - `accessToken`: YouTube API 인증에 필요한 액세스 토큰
                                          
                    **응답값**
                    - 채널 ID
                    - 사용자 채널명
                    - 업로드 영상 총 개수
                    - 구독자 수 
                    - 누적 조회수
                    """
    )
    @GetMapping("/channel-profile")
    public SuccessResponse<YouTubeChannelProfileDto> getChannelProfile(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

        // "Bearer " 제거 후 액세스 토큰만 추출
        String accessToken = authorizationHeader.substring(7);

        try {
            YouTubeChannelProfileDto profile = youTubeService.getChannelProfile(accessToken);
            return SuccessResponse.success(profile);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }
    }


    @Operation(
            summary = "조회수 상위 영상 조회",
            description = """
                    특정 채널에서 조회수 기준 상위 3개 영상을 가져옵니다.
                                              
                    **요청값**
                    - `accessToken`: YouTube API에 접근하기 위한 액세스 토큰
                    - `channelId`: 조회할 채널의 ID
                                              
                    **응답값**
                    - 상위 3개 영상의 정보 (ID, 제목, 게시일, 조회수)
                    """
    )
    /**
     * 조회수 상위 비디오 조회
     */
    @GetMapping("/top-videos")
    public SuccessResponse<List<YouTubeVideoDto>> getTopVideos(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String channelId) {

        String accessToken = authorizationHeader.substring(7);

        try {
            List<YouTubeVideoDto> topVideos = youTubeService.getTopVideos(accessToken, channelId);
            return SuccessResponse.success(topVideos);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }
    }


    @Operation(
            summary = "채널 통계 데이터 조회",
            description = """
                    지정된 기간 동안의 채널 통계 데이터를 가져옵니다.

                    **요청값**
                    - `accessToken`: YouTube API에 접근하기 위한 액세스 토큰
                    - `channelId`: 조회할 채널의 ID

                    **응답값**
                    - `YouTubeChannelStatsDto`: 채널 통계 데이터
                    """
    )
    /**
     * 기간별 채널 통계 조회
     */
    @GetMapping("/channel-stats")
    public SuccessResponse<YouTubeAnalysisResultDto> getCombinedYouTubeStats(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String channelId) {

        // "Bearer " 제거 후 액세스 토큰만 추출
        String accessToken = authorizationHeader.substring(7);

        try {
            // YouTube API 데이터 조회 및 성장률 분석 포함
            YouTubeAnalysisResultDto analysisResult = youTubeService.getCombinedStats(accessToken, channelId);

            // 캐시에 저장 (YouTubeAnalysisResultDto)
            youTubeDataCache.saveData(channelId, analysisResult);

            // YouTubeAnalysisResultDto 반환
            return SuccessResponse.success(analysisResult);

        } catch (Exception ex) {
            if (ex.getMessage().contains("invalid_token") || ex.getMessage().contains("Unauthorized")) {
                throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
            }
            throw new CustomException(ErrorCode.UNEXPECTED_ERROR, ex);
        }
    }
}
