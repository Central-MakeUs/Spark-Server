package com.example.spark.domain.strategy.api;

import com.example.spark.domain.strategy.DTO.StrategyRequestDto;
import com.example.spark.domain.strategy.service.ChatGPTService;
import com.example.spark.domain.strategy.service.GuideEmbeddingService;
import com.example.spark.domain.strategy.service.OpenAIEmbeddingService;
import com.example.spark.domain.strategy.service.PineconeService;
import com.example.spark.global.cache.StrategyCache;
import com.example.spark.global.response.ErrorResponse;
import com.example.spark.global.response.SuccessResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/pinecone")
@AllArgsConstructor
@Tag(name = "AI Strategy", description = "Pinecone Vector Search 기반 API")
public class PineconeController {
    private final ChatGPTService chatGPTService;
    private final PineconeService pineconeService;
    private final OpenAIEmbeddingService openAIEmbeddingService;
    private final GuideEmbeddingService guideEmbeddingService;
    private final StrategyCache strategyCache;
    private static final ObjectMapper objectMapper = new ObjectMapper();
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
    public Mono<ResponseEntity<Map<String, String>>> requestStrategy(
            @RequestBody StrategyRequestDto requestDto
    ) {
        String requestId = UUID.randomUUID().toString();

        String userInput = String.format(
                "내 채널 정보: 활동 분야=%s, 작업 형태=%s, 목표=%s. 주요 약점=%s, %s",
                requestDto.getActivityDomain(), requestDto.getWorkType(), requestDto.getSnsGoal(),
                requestDto.getWeaknesses().get(0), requestDto.getWeaknesses().get(1)
        );

        Mono<List<String>> matchedGuidesMono = Mono.fromCallable(() -> {
            List<Float> userEmbedding = openAIEmbeddingService.getEmbedding(userInput);
            return pineconeService.findMostRelevantGuides(userEmbedding, "default");
        }).subscribeOn(Schedulers.boundedElastic());

        Mono<Map<String, ?>> futureStrategyMono = matchedGuidesMono.flatMap(matchedGuides ->
                Mono.fromCallable(() -> chatGPTService.getGrowthStrategyForPlatform(
                                "youtube",
                                requestDto.getActivityDomain(), requestDto.getWorkType(),
                                requestDto.getSnsGoal(), requestDto.getWeaknesses(),
                                matchedGuides
                        )).onErrorResume(e -> Mono.just(Map.of("error", "전략 생성 실패: " + e.getMessage())))
                        .subscribeOn(Schedulers.boundedElastic())
        );

        // 캐시 활용하여 저장
        strategyCache.put(requestId, futureStrategyMono.toFuture());

        return Mono.just(ResponseEntity.ok(Map.of("requestId", requestId)));
    }

    @Operation(
            summary = "사용자 맞춤 메타 성장 전략 추천",
            description = "메타 네임스페이스에서 가이드를 검색해 성장 전략을 생성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공적으로 맞춤 전략을 반환",
                            content = @Content(schema = @Schema(implementation = SuccessResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 내부 오류",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/metaStrategy")
    public Mono<ResponseEntity<Map<String, String>>> requestMetaStrategy(@RequestBody StrategyRequestDto requestDto) {
        String requestId = UUID.randomUUID().toString();

        String userInput = String.format(
                "내 채널 정보: 활동 분야=%s, 작업 형태=%s, 목표=%s. 주요 약점=%s, %s",
                requestDto.getActivityDomain(), requestDto.getWorkType(), requestDto.getSnsGoal(),
                requestDto.getWeaknesses().get(0), requestDto.getWeaknesses().get(1)
        );

        Mono<List<String>> matchedGuidesMono = Mono.fromCallable(() -> {
            List<Float> userEmbedding = openAIEmbeddingService.getEmbedding(userInput);
            return pineconeService.findMostRelevantGuides(userEmbedding, "meta");
        }).subscribeOn(Schedulers.boundedElastic());

        Mono<Map<String, ?>> futureStrategyMono = matchedGuidesMono.flatMap(matchedGuides ->
                Mono.fromCallable(() -> chatGPTService.getGrowthStrategyForPlatform(
                                "meta",
                                requestDto.getActivityDomain(), requestDto.getWorkType(),
                                requestDto.getSnsGoal(), requestDto.getWeaknesses(),
                                matchedGuides
                        )).onErrorResume(e -> Mono.just(Map.of("error", "전략 생성 실패: " + e.getMessage())))
                        .subscribeOn(Schedulers.boundedElastic())
        );

        strategyCache.put(requestId, futureStrategyMono.toFuture());
        return Mono.just(ResponseEntity.ok(Map.of("requestId", requestId)));
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
    public ResponseEntity<?> getStrategy(@PathVariable String requestId) {
        CompletableFuture<Map<String, ?>> futureStrategy = strategyCache.get(requestId);

        if (futureStrategy == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "잘못된 요청 ID"));
        }

        try {
            // WebFlux 환경에서 블로킹 방식 유지
            Map<String, ?> strategy = Mono.fromFuture(futureStrategy).block();

            // 결과를 캐시에서 제거
            strategyCache.remove(requestId);
            return ResponseEntity.ok(strategy);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "전략 조회 중 오류 발생"));
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
    public ResponseEntity<SuccessResponse<String>> uploadGuidesToPinecone(
            @Parameter(name = "namespace", description = "저장할 네임스페이스. default = 유튜브, meta= 메타")
            @RequestParam(name = "namespace", defaultValue = "default") String namespace
    ) {
        try {
            guideEmbeddingService.storeTxtGuidesInPinecone(namespace);
            return ResponseEntity.ok(SuccessResponse.success("✅ 유튜브 가이드가 Pinecone에 저장되었습니다."));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(SuccessResponse.createSuccess("⚠️ 가이드 저장 중 오류 발생: " + e.getMessage()));
        }
    }
}
