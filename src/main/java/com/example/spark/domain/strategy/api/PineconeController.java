package com.example.spark.domain.strategy.api;

import com.example.spark.domain.strategy.DTO.StrategyRequestDto;
import com.example.spark.domain.strategy.service.ChatGPTService;
import com.example.spark.domain.strategy.service.GuideEmbeddingService;
import com.example.spark.domain.strategy.service.OpenAIEmbeddingService;
import com.example.spark.domain.strategy.service.PineconeService;
import com.example.spark.global.response.ErrorResponse;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/pinecone")
@AllArgsConstructor
@Tag(name = "Pinecone API", description = "Pinecone Vector Search 기반 API")
public class PineconeController {
    private final ChatGPTService chatGPTService;
    private final PineconeService pineconeService;
    private final OpenAIEmbeddingService openAIEmbeddingService;
    private final GuideEmbeddingService guideEmbeddingService;
    private final Map<String, CompletableFuture<String>> strategyCache;

    @Operation(
            summary = "사용자 맞춤 유튜브 성장 전략 추천",
            description = """
                        사용자의 활동 분야, 작업 형태, 목표, 주요 약점을 입력받아,
                        가장 유사한 유튜브 공식 가이드를 Pinecone(Vector DB)에서 검색한 후,
                        ChatGPT를 이용해 맞춤형 성장 전략을 생성하는 API입니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 맞춤 전략을 반환",
                            content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/strategy")
    public ResponseEntity<Map<String, String>> requestStrategy(@RequestBody StrategyRequestDto requestDto) {
        // 📌 요청을 식별할 UUID 생성
        String requestId = UUID.randomUUID().toString();

        // 📌 사용자 입력을 하나의 문장으로 변환
        String userInput = String.format(
                "내 채널 정보: 활동 분야=%s, 작업 형태=%s, 목표=%s. 주요 약점=%s, %s",
                requestDto.getActivityDomain(), requestDto.getWorkType(), requestDto.getSnsGoal(),
                requestDto.getWeaknesses().get(0), requestDto.getWeaknesses().get(1)
        );

        // 📌 사용자 입력을 벡터화 (OpenAI Embedding API 호출)
        List<Float> userEmbedding = openAIEmbeddingService.getEmbedding(userInput);

        // 📌 Pinecone에서 가장 유사한 가이드 3개 검색
        List<String> matchedGuides = pineconeService.findMostRelevantGuides(userEmbedding);

        // 📌 비동기적으로 ChatGPT API 호출 후 결과 저장
        CompletableFuture<String> futureStrategy = CompletableFuture.supplyAsync(() -> {
            try {
                return chatGPTService.getGrowthStrategy(
                        requestDto.getActivityDomain(), requestDto.getWorkType(),
                        requestDto.getSnsGoal(), requestDto.getWeaknesses(),
                        matchedGuides
                );
            } catch (Exception e) {
                e.printStackTrace();
                return "🚨 전략 생성 중 오류 발생!";
            }
        });

        // 캐시에 CompletableFuture 저장 (GET 요청에서 진행 상태 확인 가능)
        strategyCache.put(requestId, futureStrategy);

        // 📌 요청 ID 반환
        return ResponseEntity.ok(Map.of("requestId", requestId));
    }


    @Operation(
            summary = "Pinecone에 저장된 전략 결과 조회",
            description = "Pinecone에 저장된 전략 결과를 요청 ID로 조회하는 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 전략 결과 반환",
                            content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                    @ApiResponse(responseCode = "202", description = "전략 생성 중",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @GetMapping("/strategy/{requestId}")
    public ResponseEntity<Map<String, String>> getStrategy(@PathVariable String requestId) {
        CompletableFuture<String> futureStrategy = strategyCache.get(requestId);

        if (futureStrategy == null) {
            return ResponseEntity.ok(Map.of("strategy", "잘못된 요청 ID"));
        }

        try {
            // 🚀 결과가 나올 때까지 무조건 기다림 (ChatGPT API 응답이 올 때까지 대기)
            String strategy = futureStrategy.get();

            // ✅ 결과가 나오면 반환 후 캐시에서 제거
            strategyCache.remove(requestId);
            return ResponseEntity.ok(Map.of("strategy", strategy));
        } catch (Exception e) {
            // 🚨 기타 예외 처리 (예: InterruptedException)
            return ResponseEntity.status(500).body(Map.of("strategy", "🚨 서버 오류 발생"));
        }
    }


    @Operation(
            summary = "Pinecone에 유튜브 가이드 업로드",
            description = "Pinecone에 저장된 유튜브 가이드를 업로드하는 API입니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 가이드 업로드",
                            content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/upload-guides")
    public ResponseEntity<SuccessResponse<String>> uploadGuidesToPinecone() {
        try {
            guideEmbeddingService.storeTxtGuidesInPinecone();
            return ResponseEntity.ok(SuccessResponse.success("✅ 유튜브 가이드가 Pinecone에 저장되었습니다."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(SuccessResponse.createSuccess("⚠️ 가이드 저장 중 오류 발생: " + e.getMessage()));
        }
    }
}
