package com.navy.casualty.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navy.casualty.auth.dto.LoginRequest;
import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthController 통합 테스트.
 * 로그인/로그아웃/me API, 계정 잠금 D-02 메시지 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        User admin = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin1234"))
                .name("시스템관리자")
                .role("ADMIN")
                .enabled(true)
                .accountLocked(false)
                .failedLoginCount(0)
                .build();
        userRepository.save(admin);
    }

    @Test
    @DisplayName("로그인 성공 시 200 + 사용자 정보 반환")
    void login_success() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin1234");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.name").value("시스템관리자"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("로그인 실패 시 401 + D-02 통일 메시지")
    void login_failure_wrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("사용자 ID 또는 비밀번호가 올바르지 않습니다"));
    }

    @Test
    @DisplayName("미인증 상태에서 /api/auth/me 접근 시 401")
    void me_unauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5회 실패 후 잠금된 계정도 동일한 D-02 에러 메시지 반환")
    void login_lockedAccount_sameErrorMessage() throws Exception {
        LoginRequest wrongRequest = new LoginRequest("admin", "wrongpassword");

        // 5회 실패
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(wrongRequest)));
        }

        // 잠금 후에도 동일 메시지
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("사용자 ID 또는 비밀번호가 올바르지 않습니다"));
    }
}
