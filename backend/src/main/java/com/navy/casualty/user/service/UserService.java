package com.navy.casualty.user.service;

import java.util.List;

import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * ID로 사용자를 조회한다.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
    }

    /**
     * 전체 사용자 목록을 반환한다.
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * 새 사용자를 생성한다. 비밀번호는 BCrypt로 해싱된다.
     */
    @Transactional
    public User createUser(String username, String password, String name, String role) {
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .name(name)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    /**
     * 사용자 역할을 변경한다.
     */
    @Transactional
    public void updateRole(Long userId, String role) {
        User user = findById(userId);
        user.changeRole(role);
    }

    /**
     * 사용자 활성/비활성 상태를 토글한다.
     */
    @Transactional
    public void toggleEnabled(Long userId) {
        User user = findById(userId);
        user.toggleEnabled();
    }
}
