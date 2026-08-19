package com.sai.sai_api.domain.analysis.service;

import com.sai.sai_api.infra.ai.OpenAiClient;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;


@Service
public class OpenAiAnalysisService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OpenAiAnalysisService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public Map<String, Object> analyze(String originalText, String translatedText, String cultureCode) {
        try {
            String rawJsonResponse = openAiClient.analyzeRiskAndRefine(originalText, translatedText, cultureCode);

            //마크다운 코드블록(```json ... ```) 제거 처리
            String cleanJson = rawJsonResponse.replaceAll("```json", "").replaceAll("```", "").trim();

            return objectMapper.readValue(cleanJson, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 응답 파싱 실패: " + e.getMessage());
        }
    }
}
