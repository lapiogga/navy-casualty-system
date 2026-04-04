package com.navy.casualty.auth.dto;

import com.navy.casualty.security.CustomUserDetails;

/**
 * 로그인 응답 DTO.
 */
public record LoginResponse(String username, String name, String role, boolean passwordChanged) {

    /**
     * CustomUserDetails로부터 LoginResponse를 생성한다.
     */
    public static LoginResponse from(CustomUserDetails user) {
        return new LoginResponse(user.getUsername(), user.getName(), user.getRole(), user.isPasswordChanged());
    }
}
