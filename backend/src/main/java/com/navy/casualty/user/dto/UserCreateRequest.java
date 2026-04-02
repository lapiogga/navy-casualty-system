package com.navy.casualty.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 사용자 생성 요청 DTO.
 */
public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "ADMIN|MANAGER|OPERATOR|VIEWER") String role
) {
}
