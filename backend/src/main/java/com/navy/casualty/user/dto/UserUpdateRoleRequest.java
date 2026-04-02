package com.navy.casualty.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 역할 변경 요청 DTO.
 */
public record UserUpdateRoleRequest(
        @NotBlank @Pattern(regexp = "ADMIN|MANAGER|OPERATOR|VIEWER") String role
) {
}
