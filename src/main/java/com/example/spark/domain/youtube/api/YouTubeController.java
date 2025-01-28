package com.example.spark.domain.youtube.api;

import com.example.spark.domain.youtube.dto.YouTubeChannelProfileDto;
import com.example.spark.domain.youtube.dto.YouTubeChannelStatsDto;
import com.example.spark.domain.youtube.service.YouTubeService;
import com.example.spark.global.response.SuccessResponse;
import com.example.spark.domain.youtube.dto.YouTubeVideoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "YouTube API", description = "YouTube Analytics 데이터를 관리하는 API")
@RestController
@RequestMapping("/youtube")
public class YouTubeController {

    private final YouTubeService youTubeService;

    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }


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

        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
        YouTubeChannelProfileDto profile = youTubeService.getChannelProfile(accessToken);
        return SuccessResponse.success(profile);
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

        // Access Token 추출
        String accessToken = authorizationHeader.replace("Bearer ", "").trim();

        // 비디오 데이터 가져오기
        List<YouTubeVideoDto> topVideos = youTubeService.getTopVideos(accessToken, channelId);
        return SuccessResponse.success(topVideos);
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
    public SuccessResponse<List<YouTubeChannelStatsDto>> getChannelStats(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam String channelId) {
        // Access Token 추출
        String accessToken = authorizationHeader.replace("Bearer ", "").trim();
        List<YouTubeChannelStatsDto> stats = youTubeService.getChannelStats(accessToken, channelId);
        return SuccessResponse.success(stats);
    }
}
