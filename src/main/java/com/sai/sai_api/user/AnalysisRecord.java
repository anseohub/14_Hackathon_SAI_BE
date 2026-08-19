package com.sai.sai_api.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// 분석 이력 1건 — 마이페이지 "최근 분석" 목록에 사용
// 분석 파트(A 담당) API가 분석을 마칠 때마다 POST /api/v1/users/{id}/analyses 로 기록
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String textPreview; // 분석한 문장 (또는 앞부분)

    @Column(nullable = false)
    private String riskLevel; // "risk" | "safe"

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
