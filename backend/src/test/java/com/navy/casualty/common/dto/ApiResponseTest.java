package com.navy.casualty.common.dto;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    @DisplayName("ok() - success=true, status=200")
    void ok_returnsSuccessResponse() {
        ApiResponse<String> response = ApiResponse.ok("hello");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getErrors()).isNull();
    }

    @Test
    @DisplayName("ok(data, message) - 커스텀 메시지 포함")
    void ok_withMessage_returnsCustomMessage() {
        ApiResponse<String> response = ApiResponse.ok("data", "등록 완료");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getMessage()).isEqualTo("등록 완료");
        assertThat(response.getData()).isEqualTo("data");
    }

    @Test
    @DisplayName("error() - success=false, 지정 상태코드")
    void error_returnsErrorResponse() {
        ApiResponse<Void> response = ApiResponse.error(404, "not found");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMessage()).isEqualTo("not found");
        assertThat(response.getData()).isNull();
    }

    @Test
    @DisplayName("validationError() - status=400, errors 리스트 포함")
    void validationError_returns400WithErrors() {
        List<ApiResponse.FieldError> errors = List.of(
                new ApiResponse.FieldError("name", "필수 항목입니다"),
                new ApiResponse.FieldError("email", "이메일 형식이 아닙니다")
        );

        ApiResponse<Void> response = ApiResponse.validationError(errors);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getMessage()).isEqualTo("유효성 검증 실패");
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors().get(0).getField()).isEqualTo("name");
        assertThat(response.getErrors().get(0).getMessage()).isEqualTo("필수 항목입니다");
    }
}
