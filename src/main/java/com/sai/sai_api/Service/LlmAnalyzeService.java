package com.sai.sai_api.Service;

import com.sai.sai_api.Dto.AnalyzeRequest;
import com.sai.sai_api.Dto.AnalyzeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
@Service
@Profile("!mock")
public class LlmAnalyzeService implements AnalysisService{

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Override
    public AnalyzeResponse analyze(AnalyzeRequest req) {
        String prompt = """
            당신은 국가 간 비즈니스 커뮤니케이션의 문화적 뉘앙스를 분석하는 전문가다.
            발신자 문화권: %s / 수신자 문화권: %s / 상황: %s
            아래 한국어 메시지를 분석하라.

            메시지: "%s"

            작업:
            1. 메시지 전체를 자연스러운 영어로 번역한다.
            2. 수신자 문화권에서 오해·불쾌감·신뢰 저하를 일으킬 수 있는 위험 구간을 찾는다.
            3. 각 위험 구간에 대해 위험 제목, 문화적 이유, 더 나은 대체 표현(한국어/영어)을 제시한다.
            4. 문장에서 위험 구간을 제외한 앞부분을 koPrefix(한국어), enPrefix(영어 번역)로 분리한다.

            반드시 아래 JSON 형식으로만 답하라. 설명, 마크다운 코드블록 없이 순수 JSON만 출력하라:
            {
              "label": "%s", "sender": "%s", "receiver": "%s",
              "koPrefix": "...", "enPrefix": "...",
              "risks": [
                { "ko": "위험 구간 원문", "en": "그 부분의 영어 번역",
                  "title": "위험 요약 (15자 내외)", "reason": "문화적 이유 (2문장 이내)",
                  "fix": { "ko": "추천 한국어 문장", "en": "추천 영어 문장" } }
              ]
            }
            위험 구간이 없으면 risks를 빈 배열로 반환하라.
            """.formatted(req.receiver(), req.sender(), req.situation(), req.text(),
                req.situation(), req.sender(), req.receiver());

        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", java.util.List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        try {
            JsonNode res = restClient.post().uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve().body(JsonNode.class);
            String json = res.path("choices").get(0)
                    .path("message").path("content").asText();
            return objectMapper.readValue(json, AnalyzeResponse.class);
        } catch (Exception e) {
            throw new IllegalStateException("LLM 분석 실패: " + e.getMessage(), e);
        }
    }
}
