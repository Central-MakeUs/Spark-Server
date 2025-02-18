package com.example.spark.domain.strategy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Map;

@Service
public class ChatGPTService {
    private final WebClient webClient;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    public ChatGPTService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1/chat/completions")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String getGrowthStrategy(String activityDomain, String workType, String snsGoal, List<String> weaknesses, List<String> guides) {
        // 📌 프롬프트 생성
        String prompt = """
        크리에이터를 대상으로 유튜브 채널 성장 방법에 대한 글을 작성하고 싶습니다.
        아래의 글들은 유튜브 채널을 성장시킬 수 있는 방법을 모아둔 것입니다. 

        사용자 정보:
        - 활동 분야: %s
        - 작업 형태: %s
        - 목표: %s
        - 주요 약점: %s, %s

        위 정보를 기반으로 가장 적절한 성장 전략을 추천해주세요.
        **{{지표1}}**과 **{{지표2}}**와 관련되며, **{{전략 특징}}**과 연결되는 글을 3개 선택해주세요.
        가능하다면 **{{지표1}}**과 **{{지표2}}**를 크게 향상시킬 수 있는 전략을 중점적으로 고려해주세요.

        선택된 가이드들을 참고하여, **구체적이고 실행 가능한 3가지 성장 전략을 작성해주세요.**
        반드시 **한 가지 글로 한 가지 전략**을 제공해야 하며, 아래 예시와 같은 톤과 스타일을 유지해주세요.
        
        ✨ **스타일 가이드**
        - 딱딱하지 않고 **20대 여성이 말하듯이 부드러운 톤과 단순한 어휘**를 사용해주세요.
        - MBTI가 **T(논리적)보다는 F(감성적)**인 사람이 말하는 것처럼 작성해주세요.
        - 각 방법은 서로 **겹치지 않도록** 작성해주세요.
        - 반드시 **"제목:", "본문:", "출처:"** 형식을 유지해주세요.
        
        **[유사한 가이드 목록]**
        %s

        📌 **예시 스타일**
        
        제목 : 1. 커뮤니티 게시판 활용하기

        본문 : 유튜브 커뮤니티 게시판은 단순 홍보가 아니라 시청자와의 소통 공간으로 활용해야 해요. 팬들과 소통을 늘리면 동영상의 평균 시청시간을 늘리는 데 도움이 될 거예요.

        ✅ 실행 방법
        1. **콘텐츠 기획 참여**: "다음 영상에서 어떤 걸 다뤄볼까요?" 같은 투표를 올려보세요. 예를 들어, ‘가을 트렌드 스타일링’ vs. ‘명품 가방 입문 추천’ 중 구독자 선택이 많은 주제를 콘텐츠로 제작하면 참여율이 급상승합니다.
        2. **비하인드 공개**: "이 장면, 사실 10번 찍었어요" 같은 촬영 비하인드 컷을 올리면 팬들이 더 친근함을 느낍니다.
        3. **퀴즈·이벤트 활용**: "이 브랜드의 원래 창립 국가는 어디일까요? 정답 맞히신 분 중 3분께 소정의 선물을 드려요!" 같은 소소한 이벤트를 열어도 반응이 뜨겁습니다.

        출처 : 유튜브 [Creator Insider]

        제목 : 2. 시청자들이 직접 참여하는 콘텐츠 만들기

        본문 : 패션 유튜버로서 전문성을 강조하면서도 시청자와의 관계를 강화하려면 시청자들이 직접 참여할 수 있는 콘텐츠를 만들어야 해요. 이는 댓글 수를 늘리고 충성도 높은 팬을 확보하는 데 효과적이에요.

        ✅ 실행 방법
        1. **구독자 스타일링 리뷰**: 구독자들이 자신의 코디 사진을 보내면, 이를 분석하고 스타일링 팁을 주는 영상을 만들어보세요. 예를 들어, "OO님이 보내주신 가을 룩! 조금 더 트렌디하게 입으려면?" 같은 포맷으로 진행하면 좋습니다.
        2. **구독자 질문 코너**: "패션 고민 있으신가요? 댓글로 남겨주세요!"라고 유도한 후, 댓글에서 받은 질문을 영상에서 답변하는 Q&A 콘텐츠를 만들면 시청자들이 적극적으로 댓글을 남기게 됩니다.
        3. **스타일링 챌린지**: "5만 원 이하로 명품 느낌 나는 코디 만들기!" 같은 챌린지를 제시하고, 구독자들이 참여하도록 유도하면 자연스럽게 커뮤니티가 활성화됩니다.

        출처 : 유튜브 [Creator Insider]
        """.formatted(
                activityDomain, workType, snsGoal,
                weaknesses.get(0), weaknesses.get(1),
                String.join("\n", guides)
        );


        // OpenAI API 요청 데이터
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4-turbo",
                "messages", List.of(
                        Map.of("role", "system", "content", "유튜브 공식 가이드 기반의 성장 전략을 제공하는 AI"),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        Map<String, Object> response = webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || !response.containsKey("choices")) {
            throw new RuntimeException("ChatGPT 응답이 올바르지 않습니다.");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return "적절한 전략을 생성하지 못했습니다. 다시 시도해주세요.";
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        if (message == null || !message.containsKey("content")) {
            return "ChatGPT에서 유효한 전략 응답을 받지 못했습니다.";
        }

        return (String) message.get("content");
    }
}
