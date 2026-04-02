package com.navy.casualty.user.dto;

import java.time.LocalDateTime;

import com.navy.casualty.user.entity.User;

/**
 * 사용자 응답 DTO. password 필드를 제외한다.
 */
public record UserResponse(
        Long id,
        String username,
        String name,
        String role,
        boolean enabled,
        boolean accountLocked,
        int failedLoginCount,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt
) {

    /**
     * User 엔티티로부터 응답 DTO를 생성한다.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getRole(),
                user.isEnabled(),
                user.isAccountLocked(),
                user.getFailedLoginCount(),
                user.getLastLoginAt(),
                user.getCreatedAt()
        );
    }
}
