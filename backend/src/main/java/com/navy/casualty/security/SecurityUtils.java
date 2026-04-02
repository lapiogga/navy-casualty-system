package com.navy.casualty.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext에서 현재 인증 정보를 추출하는 유틸리티.
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 인스턴스 생성 방지
    }

    /**
     * 현재 인증된 사용자의 username을 반환한다.
     * 인증 정보가 없으면 빈 Optional을 반환한다.
     */
    public static Optional<String> getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    /**
     * 현재 인증된 사용자의 CustomUserDetails를 반환한다.
     * 인증 정보가 없으면 IllegalStateException을 던진다.
     */
    public static CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다");
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
