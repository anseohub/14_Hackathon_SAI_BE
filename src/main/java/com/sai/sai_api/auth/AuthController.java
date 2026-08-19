package com.sai.sai_api.auth;

import com.sai.sai_api.common.ApiResponse;
import com.sai.sai_api.user.UserDtos.LoginRequest;
import com.sai.sai_api.user.UserDtos.SignupRequest;
import com.sai.sai_api.user.UserDtos.UserResponse;
import com.sai.sai_api.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

// 더미 로그인: 토큰 발급 없이 유저 정보를 그대로 반환
// 프론트는 응답의 id를 저장해두고 이후 요청에 /users/{id}로 사용
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ApiResponse.ok(userService.signup(req));
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(userService.login(req));
    }
}
