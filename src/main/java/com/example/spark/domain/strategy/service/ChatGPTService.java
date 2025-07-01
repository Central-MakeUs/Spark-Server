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
        String promptTemplate = """
                당신은 크리에이터에게 유튜브 채널 성장 방법을 제안하는 AI입니다.
                        
                📌 **사용자 정보**
                - 활동 분야 (분야): {{분야}}
                - 작업 형태: {{작업형태}}
                - 목표: {{목표}}
                - 주요 약점 (지표1, 지표2): {{약점1}}, {{약점2}}
                                
                📌 **요청사항:**
                - 아래 제공된 `guides` 리스트 (Java 매개변수: `List<String> guides`)를 참고해  {{약점1}}, {{약점2}}를 개선할 수 있는 실행 비법 3가지를 작성합니다.
                - ※ guides 리스트에서 3개의 가이드를 선택하여, **가이드 1개당 비법 1개씩**, 총 3개 작성해 주세요.
                        
                📌 **작성지침:**
                1. 각각의 글을 참고해 채널을 성장시킬 수 있는 방법 1개씩 작성합니다. (총 3개)
                2. 이후, {{분야}} 유튜브 채널을 운영하면서 {{약점1}}와 {{약점2}}를 높이는 전략으로 각 글을 다시 구성합니다.
                3. 각 방법은 서로 겹치지 않게, 구체적이고 바로 실행할 수 있도록 작성합니다.
                4. 말투와 어투는 딱딱하지 않고, 20대 여성의 말투로 단순한 어휘를 기반으로 부드럽게 작성해주세요. MBTI가 T가 아닌 F인것 처럼 답변을 작성해주세요.
                5. 줄바꿈은 `\\\\n\\\\n`로 표시해주세요.
                6. 만약 선택한 활동 분야가 {{분야}}라면, 실행 방법에 {{분야}}에 관련된 용어를 연관 지어서 비법을 작성해 주세요. 예를 들어 IT라면, {콘텐츠 기획 투표: \\\\"다음 영상에서 어떤 게 좋을까요?\\\\" 같은 투표를 올려보세요. (예 : '개발자 노트북 추천' vs. '직장인 생산성 앱 소개') 투표 참여가 많을수록, 영상이 올라갔을 때 조회수도 함께 증가해요.}처럼 구체적인 예시를 반드시 포함해주세요. 만약 분야가 브이로그라면, {"실행 방법": [ "콘텐츠 기획 투표: '다음 영상에서 어떤 게 좋을까요?' 같은 투표를 올려보세요. (예: '하루 일상 브이로그' vs. '카페 탐방 브이로그') 투표 참여가 많을수록 영상 업로드 시 조회수도 함께 증가해요.", "비하인드 공개: 촬영 중 있었던 소소한 실수를 짧게 공개해보세요. (예: '카메라 끄는 걸 깜빡하고 혼잣말 다 찍힌 날:기쁨:') 인간적인 장면은 공감과 댓글 참여를 유도해요.", "소소한 이벤트 열기: 영상 내용과 연결된 퀴즈 이벤트를 진행해보세요. (예: '오늘 영상에 나온 디저트 가게 이름은?' 맞힌 분 중 2분께 카페 기프티콘 선물!)"]}
                7. 각 실행 방법에는 하단의 예시처럼 {[소제목]: [방법 설명]}형태를 사용해 주세요. 예시로는 {콘텐츠 기획 투표: "다음 영상에서 어떤 걸 다뤄볼까요?" 같은 투표를 올려보세요.}
                             
                출력 포맷:
                각 방법을 다음 JSON 형식으로 작성해 주세요.
                                
                ```json
                            {
                              "title": "<작성된 글 제목>",
                              "main text": "<작성된 본문>",
                              "source": "<원본글 출처>"
                            }
                ```
                                
                                
                이렇게 나오는 결과를 아래 양식과 같게 변경해주세요.
                📌 **출력 형식 (반드시 이 JSON 형식 유지하고 비법,제목,본문,실행 방법,출처라는 항목명 유지)**
                {
                  "비법1": {
                    "제목": "커뮤니티 게시판을 제대로 활용하기",
                    "본문": "유튜브 커뮤니티 게시판은 단순 홍보가 아니라, 구독자와 소통해 콘텐츠 성과를 높이는 핵심 채널이에요.",
                    "실행 방법": [
                      "콘텐츠 기획 투표: \\"다음 영상에서 어떤 게 좋을까요?\\" 같은 투표를 올려보세요. (예 : '개발자 노트북 추천' vs. '직장인 생산성 앱 소개') 투표 참여가 많을수록, 영상이 올라갔을 때 조회수도 함께 증가해요.",
                      "비하인드 공개: 촬영 중 마이크 오류나 코딩 실수 같은 에피소드를 짧게 공유해보세요.소소한 인간적인 모습에 댓글 반응이 활발해져요.",
                      "소소한 이벤트 열기: \\"'리눅스'의 마스코트는 어떤 동물일까요? 맞히신 분 중 3분께 개발자 스티커를 보내드려요! 퀴즈형 이벤트는 댓글수를 확 끌어올리는 효과가 있어요."
                    ],
                    "출처": "유튜브 [Creator Insider]"
                  },
                  "비법2": {
                    "제목": "시청자가 직접 참여하는 콘텐츠 만들기",
                    "본문": "IT 채널도 구독자의 참여를 유도하면, 댓글 수와 영상의 노출량이 눈에 띄게 높아져요.",
                    "실행 방법": [
                      "구독자 장비 세팅 리뷰: 구독자들이 보내준 데스크셋업 사진을 분석하며, 개선 팁을 전해주세요. 'OO님의 개발환경, 이렇게 하면 더 효율적!' 같은 식으로요. 시청자 참여형 콘텐츠는 댓글 유도에 탁월해요.",
                      "IT Q&A 받기: '코딩 공부하다 막힌 부분 있으신가요? 댓글로 질문 주세요!'라고 유도해 보세요. 댓글 수를 늘리고, 질문 기반의 후속 영상으로 자연스러운 시리즈화도 가능해요.",
                      "코딩 챌린지 열기: '30줄 이하로 간단한 웹 계산기 만들기 챌린지!' 같은 챌린지를 제안하고, 제출된 결과를 영상에서 소개하면 참여율과 댓글수 모두 급상승합니다."
                    ],
                    "출처": "유튜브 [Creator Insider]"
                  },
                  "비법3": {
                    "제목": "팬들과 상호작용을 극대화하는 댓글 전략",
                    "본문": "댓글이 많을수록 유튜브는 해당 영상을 더 많이 추천해요. 소통을 유도하는 전략적 댓글 운영이 중요해요.",
                    "실행 방법": [
                      "질문형 댓글 달기: '오늘 영상에서 가장 인상 깊었던 도구는?' 영상 말미에 질문을 던지거나 댓글로 직접 써두면, 자연스러운 댓글 참여를 유도할 수 있어요.",
                      "고정 댓글 활용: '오늘 소개한 5개 툴 중 여러분의 최애는? 아래에 남겨주세요!' 같이 영상 핵심을 요약하며 질문을 함께 넣으면, 댓글수가 눈에 띄게 증가해요.",
                      "팬 댓글 영상에 소개하기: 지난 영상에서 달린 유익한 댓글을 스크린샷으로 소개하거나 영상에 직접 활용해보세요. 팬들이 "내 댓글도 나올 수 있겠네!" 하며 더 적극적으로 참여하게 됩니다."
                    ],
                    "출처": "유튜브 [Creator Insider]"
                  }
                }
                        📌 **[유사한 가이드 목록]**
                        {{가이드목록}}
                        """;
        // 프롬프트에 변수 삽입
        String prompt = promptTemplate
                .replace("{{분야}}", activityDomain)
                .replace("{{작업형태}}", workType)
                .replace("{{목표}}", snsGoal)
                .replace("{{약점1}}", weaknesses.get(0))
                .replace("{{약점2}}", weaknesses.get(1))
                .replace("{{가이드목록}}", String.join("\n", guides));


        // OpenAI API 요청 데이터
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", List.of(
                        Map.of("role", "system", "content", "유튜브 공식 가이드 기반의 성장 전략을 제공하는 AI"),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 1.0
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
