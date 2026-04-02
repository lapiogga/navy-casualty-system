package com.navy.casualty.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.navy.casualty.auth.service.AuthService;
import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService 단위 테스트.
 * 계정 잠금(5회), 리셋, 해제 기능 검증.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .password(passwordEncoder.encode("password123"))
                .name("테스트사용자")
                .role("VIEWER")
                .enabled(true)
                .accountLocked(false)
                .failedLoginCount(0)
                .build();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("5회 실패 후 계정이 잠금된다")
    void incrementFailCount_locksAfter5Failures() {
        for (int i = 0; i < 5; i++) {
            authService.incrementFailCount("testuser");
        }

        User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updated.getFailedLoginCount()).isEqualTo(5);
        assertThat(updated.isAccountLocked()).isTrue();
    }

    @Test
    @DisplayName("4회 실패로는 잠금되지 않는다")
    void incrementFailCount_doesNotLockBefore5() {
        for (int i = 0; i < 4; i++) {
            authService.incrementFailCount("testuser");
        }

        User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updated.getFailedLoginCount()).isEqualTo(4);
        assertThat(updated.isAccountLocked()).isFalse();
    }

    @Test
    @DisplayName("resetFailCount 호출 시 실패 횟수 0, 잠금 해제")
    void resetFailCount_resetsCountAndUnlocks() {
        // 먼저 잠금 상태로 만들기
        for (int i = 0; i < 5; i++) {
            authService.incrementFailCount("testuser");
        }

        authService.resetFailCount("testuser");

        User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updated.getFailedLoginCount()).isZero();
        assertThat(updated.isAccountLocked()).isFalse();
        assertThat(updated.getLastLoginAt()).isNotNull();
    }

    @Test
    @DisplayName("unlockAccount 호출 시 잠금 해제 및 실패 횟수 0")
    void unlockAccount_unlocksAndResetsCount() {
        // 잠금 상태로 만들기
        for (int i = 0; i < 5; i++) {
            authService.incrementFailCount("testuser");
        }

        authService.unlockAccount(testUser.getId());

        User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updated.isAccountLocked()).isFalse();
        assertThat(updated.getFailedLoginCount()).isZero();
    }
}
