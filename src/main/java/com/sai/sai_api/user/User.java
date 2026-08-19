package com.sai.sai_api.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "users") // "user"는 H2 예약어라서 users로
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // 해커톤용 평문 저장 (실서비스라면 반드시 해싱)

    // ── 마이페이지에서 나중에 추가하는 업무 정보 ──
    private String company;
    private String department;
    private String position;

    // 주요 협업 국가 — 쉼표로 구분해 저장 (예: "베트남,인도네시아,미국")
    private String countries;

    // ── 활동 요약 카운터 ──
    @Builder.Default
    private int analyzedCount = 0; // 분석 문장 수

    @Builder.Default
    private int revisedCount = 0; // 추천 문장을 반영(수정)한 횟수

    @Builder.Default
    private int trainingCompletedCount = 0; // 트레이닝 완료 수

    @Builder.Default
    private int streakDays = 0; // 연속 사용일

    private LocalDate lastActiveDate; // 마지막 활동 날짜 (연속 사용일 계산용)

    public List<String> countryList() {
        if (countries == null || countries.isBlank()) return List.of();
        return Arrays.stream(countries.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    // 수정률(%) = 수정 반영 횟수 / 분석 문장 수
    public int revisionRate() {
        if (analyzedCount == 0) return 0;
        return Math.round(revisedCount * 100f / analyzedCount);
    }

    // 활동이 기록될 때마다 호출 — 연속 사용일 갱신
    public void touchActivity() {
        LocalDate today = LocalDate.now();
        if (lastActiveDate == null) {
            streakDays = 1;
        } else if (lastActiveDate.equals(today)) {
            // 오늘 이미 활동함 → 변화 없음
        } else if (lastActiveDate.equals(today.minusDays(1))) {
            streakDays += 1;
        } else {
            streakDays = 1;
        }
        lastActiveDate = today;
    }
}
