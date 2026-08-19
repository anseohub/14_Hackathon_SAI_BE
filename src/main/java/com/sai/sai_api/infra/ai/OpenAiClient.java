package com.sai.sai_api.infra.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * OpenAI API 통신을 담당하는 Client 컴포넌트
 */
@Slf4j
@Component
public class OpenAiClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 원문, 번역문, 문화권을 전달받아 OpenAI를 통한 문화적 리스크 및 수정안 분석 실행
     */
    public String analyzeRiskAndRefine(String originalText, String translatedText, String cultureCode) {
        String prompt = String.format("""
            너는 비즈니스 커뮤니케이션 및 문화적 리스크 분석 전문가야.

            [입력 정보]
            - 원문: %s
            - 1차 번역문(DeepL): %s
            - 대상 문화권: %s

            [수행할 작업]
            1차 번역문이 대상 문화권의 비즈니스 에티켓에 비추어 볼 때 무례하거나 오해를 살 수 있는지 분석해줘.
            인사말이나 부연 설명 없이, 지정된 JSON 구조 형식으로만 정확히 응답해줘.

            {
              "overallRiskLevel": "HIGH 또는 MEDIUM 또는 LOW",
              "details": [
                {
                  "startIndex": 0,
                  "endIndex": 10,
                  "originalSnippet": "위험한 문장 부분",
                  "riskLevel": "HIGH",
                  "reason": "위험 요소 설명",
                  "suggestion": "문화적으로 다듬어진 추천 수정안"
                }
              ]
            }
            """, originalText, translatedText, cultureCode);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

            if (response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            }
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage(), e);
            throw new IllegalStateException("OpenAI API 호출에 실패했습니다.", e);
        }

        throw new IllegalStateException("OpenAI 분석 응답이 비어 있습니다.");
    }

    /**
     * OpenAI Chat Completions 응답 Map 구조에서 텍스트 추출
     */
    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map responseBody) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message != null) {
                return (String) message.get("content");
            }
        }
        throw new IllegalStateException("OpenAI 응답 구조 파싱 실패");
    }
}
