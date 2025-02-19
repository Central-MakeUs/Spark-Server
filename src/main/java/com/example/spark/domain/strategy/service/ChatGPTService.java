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
        - 목표 (전략 특징): %s
        - 주요 약점 (지표1, 지표2): %s, %s
        
        📌 **전략 생성 방식**
        1. **{{지표1}}**과 **{{지표2}}**를 개선할 수 있는 글을 3개 선택해주세요.
        2. **{{전략 특징}}**과 관련된 글을 고려하여, 목표 달성에 도움이 되는 전략을 선택해주세요.
        3. **각각의 글을 참고하여 채널을 성장할 수 있는 방법 3개를 JSON 형식으로만 출력해주세요.**
        4. **각각의 전략은 다음 JSON 형식을 따르도록 해주세요.**
        5. **출력은 JSON 형식만 유지하며, 그 외의 불필요한 설명은 절대 포함하지 마세요.**
        6. **각 방법은 단순 요약이 아니라 상세한 팁과 예제를 포함해야 합니다.**
        
        📌 **출력 형식 (반드시 이 JSON 형식 유지)**
        ```json
        {
          "비법1": {
            "제목": "제목을 여기에 입력",
            "본문": "본문을 여기에 입력",
            "실행 방법": [
              "실행 방법 1 (2~3줄 이상의 상세한 설명 포함)",
              "실행 방법 2 (2~3줄 이상의 상세한 설명 포함)",
              "실행 방법 3 (2~3줄 이상의 상세한 설명 포함)"
            ],
            "출처": "출처를 여기에 입력"
          },
          "비법2": {
            "제목": "제목을 여기에 입력",
            "본문": "본문을 여기에 입력",
            "실행 방법": [
              "실행 방법 1 (2~3줄 이상의 상세한 설명 포함)",
              "실행 방법 2 (2~3줄 이상의 상세한 설명 포함)",
              "실행 방법 3 (2~3줄 이상의 상세한 설명 포함)"
            ],
            "출처": "출처를 여기에 입력"
          },
          "비법3": {
            "제목": "제목을 여기에 입력",
            "본문": "본문을 여기에 입력",
            "실행 방법": [
              "실행 방법 1 (2~3줄 이상의 상세한 설명 포함)",
              "실행 방법 2 (2~3줄 이상의 상세한 설명 포함)",
              "실행 방법 3 (2~3줄 이상의 상세한 설명 포함)"
            ],
            "출처": "출처를 여기에 입력"
          }
        }

                ✅ **반드시 JSON 형식으로만 출력하고, 그 외의 불필요한 텍스트는 포함하지 마세요.**  
                ✅ **"제목", "본문", "실행 방법", "출처" 네 개의 키를 포함해야 합니다.**  
                ✅ **"실행 방법"은 배열(List) 형식이어야 하며, 반드시 3개 이상의 실행 방법을 제공해주세요.**  
                ✅ **20대 여성이 말하는 듯한 부드러운 어투, 감성적인 F 스타일로 작성해주세요.**  
                ✅ **전략은 채널 성장을 위한 방법을 제공해야 합니다.** 
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
