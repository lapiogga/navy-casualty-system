package com.navy.casualty.integration;

import java.util.Map;

import com.navy.casualty.user.entity.User;
import com.navy.casualty.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 인증 흐름 통합 테스트.
 * 로그인 성공/실패, 계정 잠금, 비인증 접근, 로그아웃, /me 엔드포인트를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "Test1234!";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .name("테스트사용자")
                .role("ADMIN")
                .enabled(true)
                .build());
    }

    private HttpHeaders loginAndGetHeaders(String username, String password) {
        var loginDto = Map.of("username", username, "password", password);
        var resp = restTemplate.postForEntity("/api/auth/login", loginDto, String.class);
        var cookies = resp.getHeaders().get(HttpHeaders.SET_COOKIE);
        var headers = new HttpHeaders();
        if (cookies != null) {
            headers.put(HttpHeaders.COOKIE, cookies);
        }
        return headers;
    }

    @Test
    @DisplayName("로그인 성공 시 200과 세션 쿠키를 반환한다")
    void loginSuccess() {
        var loginDto = Map.of("username", USERNAME, "password", PASSWORD);
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/auth/login", loginDto, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().get(HttpHeaders.SET_COOKIE)).isNotNull();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401을 반환한다")
    void loginFailure() {
        var loginDto = Map.of("username", USERNAME, "password", "wrong");
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/auth/login", loginDto, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("5회 로그인 실패 후 계정이 잠긴다 (AUTH-02)")
    void accountLockAfterFiveFailures() {
        var loginDto = Map.of("username", USERNAME, "password", "wrong");

        for (int i = 0; i < 5; i++) {
            restTemplate.postForEntity("/api/auth/login", loginDto, String.class);
        }

        // 올바른 비밀번호로 시도해도 잠금 상태
        var correctDto = Map.of("username", USERNAME, "password", PASSWORD);
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/auth/login", correctDto, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("인증 없이 보호된 API 접근 시 401을 반환한다 (AUTH-06)")
    void unauthenticatedAccessReturns401() {
        ResponseEntity<String> resp = restTemplate.getForEntity("/api/dead", String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("로그아웃 후 세션이 무효화된다 (AUTH-07)")
    void logoutInvalidatesSession() {
        HttpHeaders headers = loginAndGetHeaders(USERNAME, PASSWORD);

        // 로그아웃
        restTemplate.exchange("/api/auth/logout", HttpMethod.POST,
                new HttpEntity<>(headers), String.class);

        // 로그아웃 후 보호된 API 접근 시 401
        ResponseEntity<String> resp = restTemplate.exchange("/api/dead",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("/api/auth/me로 현재 사용자 정보를 확인한다 (AUTH-01)")
    void meEndpointReturnsCurrentUser() {
        HttpHeaders headers = loginAndGetHeaders(USERNAME, PASSWORD);

        ResponseEntity<String> resp = restTemplate.exchange("/api/auth/me",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains(USERNAME);
    }
}
