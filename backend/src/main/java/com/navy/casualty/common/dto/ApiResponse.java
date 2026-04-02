package com.navy.casualty.common.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 공통 API 응답 래퍼.
 * D-05/D-06 형식에 맞게 JSON 직렬화된다.
 */
@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {

    private final boolean success;
    private final int status;
    private final String message;
    private final T data;
    private final List<FieldError> errors;

    /**
     * 필드 유효성 검증 오류 정보.
     */
    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private final String field;
        private final String message;
    }

    /**
     * 성공 응답 (데이터 포함).
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message("OK")
                .data(data)
                .build();
    }

    /**
     * 성공 응답 (데이터 + 메시지).
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 오류 응답.
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(status)
                .message(message)
                .build();
    }

    /**
     * 유효성 검증 오류 응답 (400).
     */
    public static <T> ApiResponse<T> validationError(List<FieldError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .status(400)
                .message("유효성 검증 실패")
                .errors(errors)
                .build();
    }
}
