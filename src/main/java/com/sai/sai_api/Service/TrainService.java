package com.sai.sai_api.Service;

import com.sai.sai_api.Dto.*;
import com.sai.sai_api.common.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TrainService {
    // language: 상대방이 사용하는 언어, role: 상대방의 역할 설명
    private record Session(String situationId, String country, String language, String role, List<MessageDto> messages) {}

    private final Map<Long, Session> sessions = new ConcurrentHashMap<>();
    // (situationId + 언어) 조합별 첫 인사말 캐시
    private final Map<String, MessageDto> openerCache = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);
    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    // 국가 선택 → 트레이닝 상대방의 언어/역할 매핑
    // 선택하지 않거나("전체") 목록에 없는 값이면 기본값(일본)으로 동작
    private static final Map<String, String[]> COUNTRY_PROFILES = Map.of(
            "일본", new String[]{"일본어", "일본 기업의 상사/동료"},
            "미국", new String[]{"영어", "미국 기업의 상사/동료"},
            "베트남", new String[]{"베트남어", "베트남 기업의 상사/동료"},
            "중국", new String[]{"중국어", "중국 기업의 상사/동료"},
            "인도네시아", new String[]{"인도네시아어", "인도네시아 기업의 상사/동료"}
    );
    private static final String[] DEFAULT_PROFILE = {"일본어", "일본 기업의 상사/동료"};

    private static final Map<String, String> CONTEXTS = Map.of(
            "meeting", "입사 첫 주, 진행 상황 공유 회의",
            "peer", "동료가 작성한 보고서 초안에서 오류를 발견한 상황",
            "boss", "상사가 제시한 마감 기한이 현실적으로 어렵다고 느껴지는 상황",
            "contract", "계약 갱신 시점에 조건 변경을 논의하는 화상 회의"
    );

    public TrainStartResponse start(TrainStartRequest req) {
        Long id = idGenerator.getAndIncrement();
        String[] profile = COUNTRY_PROFILES.getOrDefault(req.country(), DEFAULT_PROFILE);
        String language = profile[0];
        String role = profile[1];
        String context = CONTEXTS.getOrDefault(req.situationId(), CONTEXTS.get("meeting"));

        String cacheKey = req.situationId() + "|" + language;
        MessageDto opener;
        try {
            // computeIfAbsent: 이미 캐시에 있으면 OpenAI를 아예 호출하지 않는다.
            // generateOpener가 예외를 던지면 computeIfAbsent는 캐시에 아무것도 저장하지 않으므로
            // 다음 요청에서는 다시 정상적으로 시도된다.
            opener = openerCache.computeIfAbsent(cacheKey, k -> generateOpener(context, language, role));
        } catch (Exception e) {
            // 첫 인사말 생성 실패(할당량/요율 제한 초과 등)로 화면이 완전히 막히지 않도록 기본 문구로 대체
            opener = new MessageDto("ai", "...", "상대방과의 대화를 시작해보세요.", null);
        }

        List<MessageDto> history = new ArrayList<>();
        history.add(opener);
        sessions.put(id, new Session(req.situationId(), req.country(), language, role, history));
        return new TrainStartResponse(id, context, List.copyOf(history));
    }

    public TrainSendResponse send(Long sessionId, TrainSendRequest req) {
        Session session = sessions.get(sessionId);
        if (session == null) throw new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId);

        String historyText = session.messages().stream()
                .map(m -> (m.from().equals("ai") ? "상대" : "나") + ": " + m.original())
                .collect(java.util.stream.Collectors.joining("\n"));

        String prompt = """
            당신은 비즈니스 커뮤니케이션 트레이닝 시뮬레이터다. 당신의 역할: %s.
            지금까지의 대화:
            %s

            사용자가 방금 한국어로 이렇게 말했다: "%s"

            작업:
            1. 사용자의 말을 자연스러운 %s로 번역한다.
            2. 그 발화가 상대 문화권에서 어떻게 들릴지 평가하고, 문화적으로 위험하거나 어색하면
               한국어로 1~2문장의 피드백을 쓴다. 문제없으면 피드백은 null.
            3. 역할에 충실하게 상대의 다음 대사를 %s로 만들고, 한국어 번역도 붙인다.

            반드시 아래 JSON 형식으로만 답하라. 순수 JSON만 출력:
            {
              "userOriginal": "사용자 말의 %s 번역",
              "feedback": "한국어 피드백 또는 null",
              "aiOriginal": "상대의 다음 대사 (%s)",
              "aiTranslated": "그 대사의 한국어 번역"
            }
            """.formatted(session.role(), historyText, req.text(), session.language(),
                session.language(), session.language(), session.language());

        JsonNode r = callOpenAi(prompt);

        String feedback = r.path("feedback").isNull() ? null : r.path("feedback").asText();
        MessageDto userMsg = new MessageDto("user", r.path("userOriginal").asText(), req.text(), feedback);
        MessageDto aiMsg = new MessageDto("ai", r.path("aiOriginal").asText(), r.path("aiTranslated").asText(), null);

        session.messages().add(userMsg);
        session.messages().add(aiMsg);
        return new TrainSendResponse(userMsg, aiMsg);
    }

    // 세션 시작 시 상대방이 먼저 건네는 첫 대사를 국가/상황에 맞게 생성 (실패 시 예외를 그대로 던진다 — 호출부에서 캐싱 여부 판단)
    private MessageDto generateOpener(String context, String language, String role) {
        String prompt = """
            당신은 비즈니스 커뮤니케이션 트레이닝 시뮬레이터다. 당신의 역할: %s.
            상황: %s

            작업: 이 상황에서 상대방이 먼저 건넬 법한 첫 대사를 %s로 1~2문장 만들고, 한국어 번역도 붙여라.

            반드시 아래 JSON 형식으로만 답하라. 순수 JSON만 출력:
            { "original": "%s로 된 첫 대사", "translated": "그 대사의 한국어 번역" }
            """.formatted(role, context, language, language);

        JsonNode r = callOpenAi(prompt);
        return new MessageDto("ai", r.path("original").asText(), r.path("translated").asText(), null);
    }

    private JsonNode callOpenAi(String prompt) {
        String url = "https://api.openai.com/v1/chat/completions";

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        try {
            JsonNode res = restClient.post().uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(body)
                    .retrieve().body(JsonNode.class);
            String json = res.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readTree(json);
        } catch (HttpClientErrorException.TooManyRequests e) {
            // OpenAI 요율 제한/할당량 초과 — 프론트에 정확한 원인을 보여준다
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "AI_QUOTA_EXCEEDED",
                    "OpenAI API 요율 제한(또는 크레딧 소진)에 걸렸어요. 잠시 후 다시 시도하거나 크레딧/키를 확인해주세요.");
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "AI_CALL_FAILED",
                    "트레이닝 응답 생성에 실패했어요: " + e.getMessage());
        }
    }
}
