package com.navy.casualty.auth.service;

import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
     * 관리자에 의한 계정 잠금 해제.
     */
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        user.unlock();
    }
}
