package com.sai.sai_api.config;

import com.sai.sai_api.user.AnalysisRecord;
import com.sai.sai_api.user.AnalysisRecordRepository;
import com.sai.sai_api.user.User;
import com.sai.sai_api.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 서버 시작 시 시연용 계정을 자동 생성 (이메일: sudal@gmail.com / 비밀번호: 1234)
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AnalysisRecordRepository analysisRecordRepository;

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("sudal@gmail.com")) return;

        User demo = userRepository.save(User.builder()
                .name("수달")
                .email("sudal@gmail.com")
                .password("1234")
                .company("ABC CORPORATION")
                .department("글로벌사업팀")
                .position("대리")
                .countries("베트남,인도네시아,미국")
                .analyzedCount(128)
                .revisedCount(123) // 123/128 ≈ 96%
                .trainingCompletedCount(23)
                .streakDays(8)
                .lastActiveDate(LocalDate.now())
                .build());

        analysisRecordRepository.save(record(demo.getId(), "이 가격 조건은 더 이상 양보하기 어렵습니다.", "risk", 3));
        analysisRecordRepository.save(record(demo.getId(), "다른 업체를 알아볼 수밖에 없습니다.", "risk", 2));
        analysisRecordRepository.save(record(demo.getId(), "검토 부탁드립니다.", "safe", 1));
    }

    private AnalysisRecord record(Long userId, String text, String riskLevel, int hoursAgo) {
        return AnalysisRecord.builder()
                .userId(userId)
                .textPreview(text)
                .riskLevel(riskLevel)
                .createdAt(LocalDateTime.now().minusHours(hoursAgo))
                .build();
    }
}
