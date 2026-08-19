package com.sai.sai_api.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

// 요청/응답 DTO 모음
public class UserDtos {

    // ── 요청 ──

    public record SignupRequest(
            @NotBlank(message = "이름을 입력해주세요.") String name,
            @NotBlank(message = "이메일을 입력해주세요.") @Email(message = "올바른 이메일 형식이 아니에요.") String email,
            @NotBlank(message = "비밀번호를 입력해주세요.") @Size(min = 4, message = "비밀번호는 4자 이상 입력해주세요.") String password
    ) {}

    public record LoginRequest(
            @NotBlank(message = "이메일을 입력해주세요.") String email,
            @NotBlank(message = "비밀번호를 입력해주세요.") String password
    ) {}

    // 마이페이지 업무 정보 수정 — 보낸 필드만 변경 (null이면 유지)
    public record UpdateProfileRequest(
            String company,
            String department,
            String position,
            List<String> countries
    ) {}

    // 분석 활동 기록 (분석 API 담당이 분석 완료 시 호출)
    public record RecordAnalysisRequest(
            @NotBlank(message = "분석 문장이 비어 있어요.") String textPreview,
            @NotBlank(message = "riskLevel은 필수예요.")
            @Pattern(regexp = "risk|safe", message = "riskLevel은 risk 또는 safe만 가능해요.") String riskLevel,
            boolean revised // 사용자가 추천 문장을 반영했는지
    ) {}

    // ── 응답 ──

    public record UserResponse(
            Long id,
            String name,
            String email,
            String company,
            String department,
            String position,
            List<String> countries
    ) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getName(), u.getEmail(),
                    u.getCompany(), u.getDepartment(), u.getPosition(), u.countryList());
        }
    }

    public record StatsResponse(
            int analyzedCount,
            int revisionRate,
            int completedTrainingCount,
            int streakDays
    ) {
        public static StatsResponse from(User u) {
            return new StatsResponse(u.getAnalyzedCount(), u.revisionRate(),
                    u.getTrainingCompletedCount(), u.getStreakDays());
        }
    }

    public record AnalysisItem(Long id, String textPreview, String riskLevel, LocalDateTime createdAt) {
        public static AnalysisItem from(AnalysisRecord r) {
            return new AnalysisItem(r.getId(), r.getTextPreview(), r.getRiskLevel(), r.getCreatedAt());
        }
    }
}
