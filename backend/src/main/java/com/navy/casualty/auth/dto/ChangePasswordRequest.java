package com.navy.casualty.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 변경 요청 DTO.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "현재 비밀번호를 입력하세요")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력하세요")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).+$",
                message = "비밀번호는 영문과 숫자를 포함해야 합니다")
        String newPassword
) {}
