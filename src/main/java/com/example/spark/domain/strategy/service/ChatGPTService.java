package com.example.spark.domain.strategy.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public Map<String, Object> getGrowthStrategy(String activityDomain, String workType, String snsGoal, List<String> weaknesses, List<String> guides) {
        // 📌 프롬프트 생성
        String prompt = """
                크리에이터를 대상으로 유튜브 채널 성장 방법에 대한 글을 작성하고 싶습니다.  
                아래의 글들은 유튜브 채널을 성장시킬 수 있는 방법을 모아둔 것입니다.
                        
                📌 **사용자 정보**
                - 활동 분야 (분야): %s
                - 작업 형태: %s
                - 목표: %s
                - 주요 약점 (지표1, 지표2): %s, %s
                        
                📌 **전략 생성 방식**
                1. **{{지표1}}**과 **{{지표2}}**를 개선할 수 있는 글을 3개 선택해주세요.
                2. **{{목표}}**과 관련지어서, 목표 달성에 도움이 되는 전략을 작성해주세요.
                3. **각각의 글을 참고하여 채널을 성장할 수 있는 방법 3개를 JSON 형식으로만 출력해주세요.**
                4. **각각의 전략은 다음 JSON 형식을 따르도록 해주세요.**
                5. **출력은 JSON 형식만 유지하며, 그 외의 불필요한 설명은 절대 포함하지 마세요.**
                6. **아래 아래 예시를 참고해서 비슷한 톤과 스타일을 유지해 주세요.**
                7. 말투와 어투는 딱딱하지 않고, 20대 여성의 말투로 단순한 어휘를 기반으로 부드럽게 작성해주세요. MBTI가 T가 아닌 F인것 처럼 답변을 작성해주세요.
                8. 실행 방법은 방법 : "내용" 형식으로 작성해주세요. (예시 참고)  
                            
                📌 **예시**             
                제목 : 1. 커뮤니티 게시판 활용하기                               
                본문 : 유튜브 커뮤니티 게시판은 단순 홍보가 아니라 시청자와의 소통 공간으로 활용해야 해요. 팬들과 소통을 늘리면 동영상의 평균 시청시간을 늘리는 데 도움이 될 거예요.                 
                실행방법 :                            
                1. 콘텐츠 기획 참여: "다음 영상에서 어떤 걸 다뤄볼까요?" 같은 투표를 올려보세요. 예를 들어, ‘가을 트렌드 스타일링’ vs. ‘명품 가방 입문 추천’ 중 구독자 선택이 많은 주제를 콘텐츠로 제작하면 참여율이 급상승합니다.
                2. 비하인드 공개: "이 장면, 사실 10번 찍었어요" 같은 촬영 비하인드 컷을 올리면 팬들이 더 친근함을 느낍니다.
                3. 퀴즈·이벤트 활용: "이 브랜드의 원래 창립 국가는 어디일까요? 정답 맞히신 분 중 3분께 소정의 선물을 드려요!" 같은 소소한 이벤트를 열어도 반응이 뜨겁습니다.           
                출처 : 유튜브 [Creator Insider]
                                
                제목 : 2. 시청자들이 직접 참여하는 콘텐츠 만들기     
                본문 : 패션 유튜버로서 전문성을 강조하면서도 시청자와의 관계를 강화하려면 시청자들이 직접 참여할 수 있는 콘텐츠를 만들어야 해요. 이는 댓글 수를 늘리고 충성도 높은 팬을 확보하는 데 효과적이에요.                     
                실행방법 :          
                1. 구독자 스타일링 리뷰: 구독자들이 자신의 코디 사진을 보내면, 이를 분석하고 스타일링 팁을 주는 영상을 만들어보세요. 예를 들어, "OO님이 보내주신 가을 룩! 조금 더 트렌디하게 입으려면?" 같은 포맷으로 진행하면 좋습니다.
                2. 구독자 질문 코너: "패션 고민 있으신가요? 댓글로 남겨주세요!"라고 유도한 후, 댓글에서 받은 질문을 영상에서 답변하는 Q&A 콘텐츠를 만들면 시청자들이 적극적으로 댓글을 남기게 됩니다.
                3. 스타일링 챌린지: "5만 원 이하로 명품 느낌 나는 코디 만들기!" 같은 챌린지를 제시하고, 구독자들이 참여하도록 유도하면 자연스럽게 커뮤니티가 활성화됩니다.                         
                출처 : 유튜브 [Creator Insider]
            
                제목 : 3. 팬들과의 상호작용을 극대화하는 댓글 전략              
                본문 : 댓글이 활성화되면 유튜브 알고리즘이 영상의 인게이지먼트(engagement)가 높다고 판단해 추천에 자주 노출시켜 줍니다. 단순한 답글이 아니라 전략적으로 팬들과 소통하면 댓글 수도 자연스럽게 증가해요.               
                실행 방법 :                           
                1. 댓글에 질문 던지기: 단순한 감사 인사보다 "이번 영상에서 가장 유용했던 팁은?" 같은 질문을 던지면 댓글 참여율이 올라갑니다.
                2. 고정 댓글 활용: 영상의 중요한 내용을 요약한 후, "여러분은 어떤 스타일을 더 선호하시나요?" 같은 질문을 고정 댓글로 남기면 시청자들이 답글을 남기기 쉽습니다.
                3. 팬 댓글을 영상으로 소개: "지난 영상에서 OOO님이 이런 팁을 주셨는데 너무 유용해서 공유합니다!" 같은 방식으로 팬의 댓글을 영상에서 직접 언급하면 시청자들이 더 적극적으로 댓글을 남기게 됩니다.               
                출처 : 유튜브 [Creator Insider]
                
                
                이렇게 나오는 결과를 아래 양식과 같게 변경해줘
                📌 **출력 형식 (반드시 이 JSON 형식 유지하고 비법,제목,본문,실행 방법,출처라는 항목명 유지)**
                {
                "비법1": {
                "제목": "커뮤니티 게시판 활용하기",
                "본문": "유튜브 커뮤니티 게시판은 단순 홍보가 아니라 시청자와의 소통 공간으로 활용해야 해요. 팬들과 소통을 늘리면 동영상의 평균 시청시간을 늘리는 데 도움이 될 거예요.",
                "실행 방법": [
                "콘텐츠 기획 참여: '다음 영상에서 어떤 걸 다뤄볼까요?' 같은 투표를 올려보세요. 예를 들어, ‘가을 트렌드 스타일링’ vs. ‘명품 가방 입문 추천’ 중 구독자 선택이 많은 주제를 콘텐츠로 제작하면 참여율이 급상승합니다.",
                "비하인드 공개: '이 장면, 사실 10번 찍었어요' 같은 촬영 비하인드 컷을 올리면 팬들이 더 친근함을 느낍니다.",
                "퀴즈·이벤트 활용: '이 브랜드의 원래 창립 국가는 어디일까요? 정답 맞히신 분 중 3분께 소정의 선물을 드려요!' 같은 소소한 이벤트를 열어도 반응이 뜨겁습니다."
                ],
                "출처": "유튜브 [Creator Insider]"
                }
            
                        📌 **[유사한 가이드 목록]**
                        %s
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
            throw new RuntimeException("적절한 전략을 생성하지 못했습니다. 다시 시도해주세요.");
        }

        Map<String, Object> firstChoice = choices.get(0);
        Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");

        if (message == null || !message.containsKey("content")) {
            throw new RuntimeException("ChatGPT에서 유효한 전략 응답을 받지 못했습니다.");
        }

        String jsonResponse = (String) message.get("content");

        // 🔍 ChatGPT 응답 확인
        System.out.println("🔍 ChatGPT 응답: " + jsonResponse);

        // 불필요한 백틱(```json`) 제거
        jsonResponse = jsonResponse.replaceAll("^```json|```$", "").trim();

        try {
            // JSON 문자열을 Map으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> parsedResponse = objectMapper.readValue(jsonResponse, Map.class);

            // ✅ "실행 방법" 필드가 문자열일 경우 리스트로 변환하여 JSON 정상화
            for (String key : parsedResponse.keySet()) {
                Map<String, Object> strategy = (Map<String, Object>) parsedResponse.get(key);
                Object executionSteps = strategy.get("실행 방법");

                if (executionSteps instanceof String) {
                    strategy.put("실행 방법", List.of(((String) executionSteps).split("\n")));
                }
            }

            return parsedResponse;
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 오류: " + e.getMessage() + "\n응답 내용: " + jsonResponse);
        }
    }
}
