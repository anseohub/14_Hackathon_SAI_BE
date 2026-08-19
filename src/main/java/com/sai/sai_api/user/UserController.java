package com.sai.sai_api.user;

import com.sai.sai_api.common.ApiResponse;
import com.sai.sai_api.user.UserDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 마이페이지 프로필
    @GetMapping("/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getUser(userId));
    }

    // 업무 정보/문화 프로필 수정 (보낸 필드만 변경)
    @PatchMapping("/{userId}")
    public ApiResponse<UserResponse> updateProfile(@PathVariable Long userId,
                                                   @RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(userId, req));
    }

    // 회원 탈퇴 (유저 + 분석 이력 삭제)
    @DeleteMapping("/{userId}")
    public ApiResponse<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.ok(null);
    }

    // 내 활동 요약 (분석 문장 / 수정률 / 완료 / 연속 사용일)
    @GetMapping("/{userId}/stats")
    public ApiResponse<StatsResponse> getStats(@PathVariable Long userId) {
        return ApiResponse.ok(userService.getStats(userId));
    }

    // 최근 분석 목록 (마이페이지 카드: size=3)
    @GetMapping("/{userId}/analyses")
    public ApiResponse<List<AnalysisItem>> getRecentAnalyses(@PathVariable Long userId,
                                                             @RequestParam(defaultValue = "3") int size) {
        return ApiResponse.ok(userService.getRecentAnalyses(userId, size));
    }

    // ── 활동 기록: 분석/트레이닝 파트가 완료 시점에 호출 → 수치 증가 ──

    @PostMapping("/{userId}/analyses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StatsResponse> recordAnalysis(@PathVariable Long userId,
                                                     @Valid @RequestBody RecordAnalysisRequest req) {
        return ApiResponse.ok(userService.recordAnalysis(userId, req));
    }

    @PostMapping("/{userId}/trainings/complete")
    public ApiResponse<StatsResponse> completeTraining(@PathVariable Long userId) {
        return ApiResponse.ok(userService.completeTraining(userId));
    }
}
