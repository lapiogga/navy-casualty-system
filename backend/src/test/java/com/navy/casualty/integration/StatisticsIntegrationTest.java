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
 * 통계 API 통합 테스트.
 * 6종 통계 API 호출, 응답 구조, 응답 시간 5초 이내 검증.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StatisticsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private HttpHeaders authHeaders;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("Admin1234!"))
                .name("관리자")
                .role("ADMIN")
                .enabled(true)
                .build());
        authHeaders = loginAndGetHeaders("admin", "Admin1234!");
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
    @DisplayName("신분별 통계 API 응답 시간이 5초 이내다 (STAT-07)")
    void branchStatWithin5Seconds() {
        long start = System.currentTimeMillis();
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/statistics/branch", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(elapsed).isLessThan(5000);
    }

    @Test
    @DisplayName("월별 통계 API 응답 시간이 5초 이내다")
    void monthlyStatWithin5Seconds() {
        long start = System.currentTimeMillis();
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/statistics/monthly", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(elapsed).isLessThan(5000);
    }

    @Test
    @DisplayName("연도별 통계 API가 200을 반환한다")
    void yearlyStatReturns200() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/statistics/yearly", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("data");
    }

    @Test
    @DisplayName("부대별 통계 API 응답 시간이 5초 이내다")
    void unitStatWithin5Seconds() {
        long start = System.currentTimeMillis();
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/statistics/unit", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(elapsed).isLessThan(5000);
    }

    @Test
    @DisplayName("전사망자 명부 API가 200을 반환한다")
    void rosterAllReturns200() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/statistics/roster/all", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("data");
    }

    @Test
    @DisplayName("부대별 명부 API가 unitId 파라미터로 200을 반환한다")
    void rosterByUnitReturns200() {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/statistics/roster/unit?unitId=1", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
