package com.sai.sai_api.common;

// 모든 API가 공통으로 사용하는 응답 래퍼
// 성공: { "success": true, "data": {...} }
// 실패: { "success": false, "error": { "code": "...", "message": "..." } }
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorBody(code, message));
    }

    public record ErrorBody(String code, String message) {}
}
