package com.example.spark.domain.strategy.api;

import com.example.spark.domain.strategy.DTO.StrategyRequestDto;
import com.example.spark.domain.strategy.service.ChatGPTService;
import com.example.spark.domain.strategy.service.GuideEmbeddingService;
import com.example.spark.domain.strategy.service.PineconeService;
import com.example.spark.domain.strategy.service.OpenAIEmbeddingService;
import com.example.spark.global.response.ErrorResponse;
import com.example.spark.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.io.IOException;

@RestController
@RequestMapping("/pinecone")
@AllArgsConstructor
@Tag(name = "Pinecone API", description = "Pinecone Vector Search 기반 API")
public class PineconeController {
    private final ChatGPTService chatGPTService;
    private final PineconeService pineconeService;
    private final OpenAIEmbeddingService openAIEmbeddingService;
    private final GuideEmbeddingService guideEmbeddingService;

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
    public ResponseEntity<Map<String, String>> getStrategy(@RequestBody StrategyRequestDto requestDto) {

        // 📌 사용자 입력을 하나의 문장으로 변환
        String userInput = String.format(
                "내 채널 정보: 활동 분야=%s, 작업 형태=%s, 목표=%s. 주요 약점=%s, %s",
                requestDto.getActivityDomain(), requestDto.getWorkType(), requestDto.getSnsGoal(),
                requestDto.getWeaknesses().get(0), requestDto.getWeaknesses().get(1)
        );

        // 📌 사용자 입력을 벡터화
        List<Float> userEmbedding = openAIEmbeddingService.getEmbedding(userInput);
        // 📌 Pinecone에서 가장 유사한 가이드 3개 검색
        List<String> matchedGuides = pineconeService.findMostRelevantGuides(userEmbedding);
        System.out.println("📌 Pinecone에서 가장 유사한 가이드: " + matchedGuides);
        // 📌 ChatGPT API를 호출하여 맞춤형 전략 생성
        String strategy = chatGPTService.getGrowthStrategy(
                requestDto.getActivityDomain(), requestDto.getWorkType(),
                requestDto.getSnsGoal(), requestDto.getWeaknesses(),
                matchedGuides
        );
        System.out.println("📌 ChatGPT를 통한 맞춤 전략 생성: " + strategy);
        // 📌 결과 반환
        return ResponseEntity.ok(Map.of("strategy", strategy));
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
