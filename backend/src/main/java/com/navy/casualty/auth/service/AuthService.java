package com.navy.casualty.auth.service;

import com.navy.casualty.security.CustomUserDetails;
import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직.
 * 로그인 실패 횟수 관리, 계정 잠금/해제를 담당한다.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로그인 실패 횟수를 1 증가시킨다.
     * 5회 이상이면 계정을 잠근다.
     */
    @Transactional
    public void incrementFailCount(String username) {
        userRepository.findByUsername(username).ifPresent(User::incrementFailCount);
    }

    /**
     * 로그인 성공 시 실패 횟수를 초기화한다.
     */
    @Transactional
    public void resetFailCount(String username) {
        userRepository.findByUsername(username).ifPresent(User::resetFailCount);
    }

    /**
     * 비밀번호를 변경한다.
     * 현재 비밀번호 검증 후 새 비밀번호로 업데이트하고 passwordChanged 플래그를 true로 설정한다.
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다");
        }

        user.changePassword(passwordEncoder.encode(newPassword));

        // SecurityContext의 인증 정보를 갱신하여 passwordChanged 반영
        CustomUserDetails updated = CustomUserDetails.from(user);
        var auth = new UsernamePasswordAuthenticationToken(
                updated, updated.getPassword(), updated.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    /**
     * 관리자에 의한 계정 잠금 해제.
     */
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.unlock();
    }
}
