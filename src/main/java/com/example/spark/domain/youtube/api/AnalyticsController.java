package com.example.spark.domain.youtube.api;
import com.example.spark.domain.youtube.service.ChatGPTService;
import com.example.spark.domain.youtube.service.FlaskService;
import com.example.spark.domain.youtube.service.YouTubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Analytics API", description = "YouTube Analytics API")
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final YouTubeService youTubeService;
    private final FlaskService flaskService;
    private final ChatGPTService chatGPTService;

    public AnalyticsController(YouTubeService youTubeService, FlaskService flaskService, ChatGPTService chatGPTService) {
        this.youTubeService = youTubeService;
        this.flaskService = flaskService;
        this.chatGPTService = chatGPTService;
    }

//    @Operation(
//            summary = "성장 전략 조회",
//            description = """
//                          YouTube 데이터 및 Flask 기반 머신러닝 결과를 활용하여 성장 전략을 생성합니다.
//
//                          **작업 흐름**
//                          1. YouTube API를 호출하여 채널 데이터를 가져옵니다.
//                          2. Flask 서버로 채널 데이터를 전송하여 성장 예측 데이터를 생성합니다.
//                          3. ChatGPT API를 호출하여 성장 전략을 생성합니다.
//
//                          **응답값**
//                          - 성장 전략에 대한 텍스트
//                          """
//    )
//    @GetMapping("/growth-strategy")
//    public String getGrowthStrategy() {
//        // 1. YouTube API 데이터 호출
//        String channelData = youTubeService.getChannelStats("2024-12-01", "2024-12-31").toString();
//
//        // 2. Flask로 머신러닝 예측 호출
//        String prediction = flaskService.getGrowthPrediction(channelData);
//
//        // 3. ChatGPT API로 전략 생성
//        return chatGPTService.generateStrategy(prediction);
//    }
}
