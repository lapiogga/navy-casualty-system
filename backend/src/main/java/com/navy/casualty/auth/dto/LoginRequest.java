package com.navy.casualty.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO.
 */
public record LoginRequest(
        @NotBlank(message = "사용자 ID를 입력하세요") String username,
        @NotBlank(message = "비밀번호를 입력하세요") String password
) {}
