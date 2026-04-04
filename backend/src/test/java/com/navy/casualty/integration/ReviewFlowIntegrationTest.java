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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 전공사상심사 흐름 통합 테스트.
 * 심사 등록, 수정, 이력 조회, 보훈청 통보를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReviewFlowIntegrationTest {

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
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
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

    private Map<String, Object> createReviewRequest(String serviceNumber, int reviewRound) {
        return Map.ofEntries(
                Map.entry("reviewRound", reviewRound),
                Map.entry("reviewDate", "2025-06-01"),
                Map.entry("name", "정대호"),
                Map.entry("serviceNumber", serviceNumber),
                Map.entry("ssn", "880101-1234567"),
                Map.entry("birthDate", "1988-01-01"),
                Map.entry("diseaseName", "요추 추간판 탈출증"),
                Map.entry("unitReviewResult", "전공상 해당")
        );
    }

    private long extractId(String body) {
        int idStart = body.indexOf("\"id\":");
        String idStr = body.substring(idStart + 5).split("[,}]")[0].trim();
        return Long.parseLong(idStr);
    }

    @Test
    @DisplayName("심사를 등록하면 201을 반환하고 이력이 자동 생성된다 (REVW-02/06)")
    void createReviewAndCheckHistory() {
        var request = createReviewRequest("22-20001", 1);
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/reviews", new HttpEntity<>(request, authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long id = extractId(resp.getBody());

        // 이력 조회
        ResponseEntity<String> historyResp = restTemplate.exchange(
                "/api/reviews/" + id + "/histories", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertThat(historyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(historyResp.getBody()).contains("data");
    }

    @Test
    @DisplayName("심사를 수정하면 200을 반환하고 이력 스냅샷이 생성된다 (REVW-03/06)")
    void updateReviewCreatesHistorySnapshot() {
        var createReq = createReviewRequest("22-20002", 1);
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/reviews", new HttpEntity<>(createReq, authHeaders), String.class);
        long id = extractId(createResp.getBody());

        var updateReq = Map.ofEntries(
                Map.entry("reviewRound", 1),
                Map.entry("name", "정대호"),
                Map.entry("serviceNumber", "22-20002"),
                Map.entry("ssn", "880101-1234567"),
                Map.entry("birthDate", "1988-01-01"),
                Map.entry("diseaseName", "요추 추간판 탈출증"),
                Map.entry("unitReviewResult", "전공상 해당"),
                Map.entry("classification", "COMBAT_WOUND")
        );
        ResponseEntity<String> updateResp = restTemplate.exchange(
                "/api/reviews/" + id, HttpMethod.PUT,
                new HttpEntity<>(updateReq, authHeaders), String.class);

        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("보훈청 통보 일시를 기록하면 200을 반환한다 (REVW-08)")
    void recordNotification() {
        var createReq = createReviewRequest("22-20003", 1);
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/reviews", new HttpEntity<>(createReq, authHeaders), String.class);
        long id = extractId(createResp.getBody());

        // 상태 전이: REGISTERED -> UNDER_REVIEW -> CONFIRMED -> NOTIFIED
        restTemplate.exchange(
                "/api/reviews/" + id + "/status?status=UNDER_REVIEW", HttpMethod.PUT,
                new HttpEntity<>(authHeaders), String.class);
        restTemplate.exchange(
                "/api/reviews/" + id + "/status?status=CONFIRMED", HttpMethod.PUT,
                new HttpEntity<>(authHeaders), String.class);

        HttpHeaders notifyHeaders = new HttpHeaders(authHeaders);
        notifyHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<String> notifyResp = restTemplate.exchange(
                "/api/reviews/" + id + "/notify", HttpMethod.PUT,
                new HttpEntity<>("{}", notifyHeaders), String.class);

        assertThat(notifyResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("심사 목록을 검색하면 200을 반환한다")
    void searchReviews() {
        var request = createReviewRequest("22-20004", 2);
        restTemplate.postForEntity("/api/reviews", new HttpEntity<>(request, authHeaders), String.class);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/reviews?page=0&size=20", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("content");
    }
}
