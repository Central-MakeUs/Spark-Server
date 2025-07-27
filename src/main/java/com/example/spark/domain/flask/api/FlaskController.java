package com.example.spark.domain.flask.api;

import com.example.spark.domain.youtube.dto.YouTubeAnalysisResultDto;
import com.example.spark.domain.youtube.service.YouTubeDataCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Flask", description = "Flask 서버와의 데이터 전송 API (Deprecated)")
@RestController
@RequiredArgsConstructor
@Deprecated
public class FlaskController {
    private final YouTubeDataCache youTubeDataCache;
    private final RestTemplate restTemplate = new RestTemplate();

    @Operation(
            summary = "YouTube Analytics 데이터 전송 (Deprecated)",
            description = """
                    YouTube Analytics 데이터를 Flask 서버로 전송합니다.
                    추가적으로 사용자 정보 기입 페이지에서 받은 데이터도 함께 전송합니다.
                    
                    **요청값**
                    - `channelId`: 조회할 채널 ID
                    - `userInfo`: 입력받은 사용자 정보
                    
                    **응답값**
                    - 상태 메세지
                    - 입력받은 사용자 정보
                    - 채널 통계 데이터
                    
                    **참고**
                    - Flask 서버 URL: http://221.147.110.182:5000/
                    - **⚠️ 이 API는 더 이상 사용되지 않습니다. 향후 버전에서 제거될 예정입니다.**

                    """,
            deprecated = true
    )
    @PostMapping("/relay-youtube-analytics")
    public ResponseEntity<Map<String, Object>> relayYouTubeAnalyticsToFlask(
            @RequestParam String channelId,
            @RequestParam String userInfo
    ) {
        // 캐시에서 데이터 조회
        YouTubeAnalysisResultDto youtubeData = youTubeDataCache.getAndRemoveData(channelId);
        if (youtubeData == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No data found for channelId: " + channelId));
        }

        // Flask로 전송할 데이터 구성
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("youtubeData", youtubeData);
        requestData.put("userInfo", userInfo);

        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody;
        try {
            requestBody = objectMapper.writeValueAsString(requestData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 오류", e);
        }

        // Flask로 HTTP 요청 전송
        String flaskUrl = "http://221.147.110.182:5000/relay-youtube-analytics";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(flaskUrl, HttpMethod.POST, requestEntity, String.class);

        // Flask 응답을 JSON으로 변환
        Map<String, Object> flaskResponse;
        try {
            flaskResponse = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            flaskResponse = Map.of("flaskResponse", response.getBody(), "error", "Failed to parse Flask response");
        }

        // 최종 응답 데이터 구성 (Flask 응답 + 전송된 JSON 데이터 포함)
        Map<String, Object> finalResponse = new HashMap<>();
        finalResponse.put("flaskResponse", flaskResponse);
        finalResponse.put("sentData", requestData);

        return ResponseEntity.ok(finalResponse);
    }
}



