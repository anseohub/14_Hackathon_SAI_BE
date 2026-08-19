package com.sai.sai_api.user;

import com.sai.sai_api.common.ApiException;
import com.sai.sai_api.user.UserDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AnalysisRecordRepository analysisRecordRepository;

    // ── 인증 (더미: 토큰 없이 유저 정보만 반환) ──

    @Transactional
    public UserResponse signup(SignupRequest req) {
        String email = req.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_DUPLICATED", "이미 가입된 이메일이에요.");
        }
        User user = User.builder()
                .name(req.name().trim())
                .email(email)
                .password(req.password())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "EMAIL_NOT_FOUND", "가입되지 않은 이메일이에요."));
        if (!user.getPassword().equals(req.password())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "PASSWORD_MISMATCH", "비밀번호가 일치하지 않아요.");
        }
        return UserResponse.from(user);
    }

    // ── 마이페이지 ──

    @Transactional(readOnly = true)
    public UserResponse getUser(Long userId) {
        return UserResponse.from(findUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest req) {
        User user = findUser(userId);
        if (req.company() != null) user.setCompany(req.company().trim());
        if (req.department() != null) user.setDepartment(req.department().trim());
        if (req.position() != null) user.setPosition(req.position().trim());
        if (req.countries() != null) user.setCountries(String.join(",", req.countries()));
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public StatsResponse getStats(Long userId) {
        return StatsResponse.from(findUser(userId));
    }

    @Transactional(readOnly = true)
    public List<AnalysisItem> getRecentAnalyses(Long userId, int size) {
        findUser(userId); // 존재 확인
        return analysisRecordRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, size))
                .stream().map(AnalysisItem::from).toList();
    }

    // ── 활동 기록 (다른 파트 API가 호출 → 활동 요약 수치가 실제로 올라감) ──

    @Transactional
    public StatsResponse recordAnalysis(Long userId, RecordAnalysisRequest req) {
        User user = findUser(userId);
        analysisRecordRepository.save(AnalysisRecord.builder()
                .userId(userId)
                .textPreview(req.textPreview())
                .riskLevel(req.riskLevel())
                .createdAt(LocalDateTime.now())
                .build());
        user.setAnalyzedCount(user.getAnalyzedCount() + 1);
        if (req.revised()) user.setRevisedCount(user.getRevisedCount() + 1);
        user.touchActivity();
        return StatsResponse.from(user);
    }

    @Transactional
    public StatsResponse completeTraining(Long userId) {
        User user = findUser(userId);
        user.setTrainingCompletedCount(user.getTrainingCompletedCount() + 1);
        user.touchActivity();
        return StatsResponse.from(user);
    }

    // 회원 탈퇴 — 유저와 분석 이력을 함께 삭제
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUser(userId);
        analysisRecordRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "유저를 찾을 수 없어요."));
    }
}
