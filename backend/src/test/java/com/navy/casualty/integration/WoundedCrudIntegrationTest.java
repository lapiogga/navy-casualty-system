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
 * 상이자 CRUD 통합 테스트.
 * 등록/조회/수정/삭제/중복방지를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WoundedCrudIntegrationTest {

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

    private Map<String, Object> createWoundedRequest(String serviceNumber, String ssn) {
        return Map.ofEntries(
                Map.entry("serviceNumber", serviceNumber),
                Map.entry("name", "이영희"),
                Map.entry("ssn", ssn),
                Map.entry("birthDate", "1992-05-20"),
                Map.entry("woundType", "COMBAT_WOUND"),
                Map.entry("address", "인천시 연수구")
        );
    }

    @Test
    @DisplayName("로그인 후 상이자를 등록하면 201을 반환한다")
    void createWounded() {
        var request = createWoundedRequest("21-10001", "920520-1234567");
        var entity = new HttpEntity<>(request, authHeaders);

        ResponseEntity<String> resp = restTemplate.postForEntity("/api/wounded", entity, String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @DisplayName("상이자 목록을 조회하면 200과 페이징 결과를 반환한다")
    void searchWounded() {
        var request = createWoundedRequest("21-10002", "920620-1234567");
        restTemplate.postForEntity("/api/wounded", new HttpEntity<>(request, authHeaders), String.class);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/wounded?page=0&size=20", HttpMethod.GET,
                new HttpEntity<>(authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("content");
    }

    @Test
    @DisplayName("상이자 정보를 수정하면 200을 반환한다")
    void updateWounded() {
        var createReq = createWoundedRequest("21-10003", "920720-1234567");
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/wounded", new HttpEntity<>(createReq, authHeaders), String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        String body = createResp.getBody();
        assertThat(body).isNotNull();
        int idStart = body.indexOf("\"id\":");
        String idStr = body.substring(idStart + 5).split("[,}]")[0].trim();
        long id = Long.parseLong(idStr);

        var updateReq = Map.ofEntries(
                Map.entry("name", "박영미"),
                Map.entry("ssn", "920720-1234567"),
                Map.entry("birthDate", "1992-07-20"),
                Map.entry("woundType", "DUTY_WOUND"),
                Map.entry("address", "부산시 수영구")
        );
        ResponseEntity<String> updateResp = restTemplate.exchange(
                "/api/wounded/" + id, HttpMethod.PUT,
                new HttpEntity<>(updateReq, authHeaders), String.class);

        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("상이자를 논리 삭제하면 200을 반환한다")
    void deleteWounded() {
        var createReq = createWoundedRequest("21-10004", "920820-1234567");
        ResponseEntity<String> createResp = restTemplate.postForEntity(
                "/api/wounded", new HttpEntity<>(createReq, authHeaders), String.class);

        String body = createResp.getBody();
        assertThat(body).isNotNull();
        int idStart = body.indexOf("\"id\":");
        String idStr = body.substring(idStart + 5).split("[,}]")[0].trim();
        long id = Long.parseLong(idStr);

        var deleteReq = Map.of("reason", "테스트 삭제 사유");
        ResponseEntity<String> deleteResp = restTemplate.exchange(
                "/api/wounded/" + id, HttpMethod.DELETE,
                new HttpEntity<>(deleteReq, authHeaders), String.class);

        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("동일 주민번호로 중복 등록 시 409를 반환한다")
    void duplicateRegistrationReturns409() {
        var request1 = createWoundedRequest("21-10005", "920920-1234567");
        restTemplate.postForEntity("/api/wounded", new HttpEntity<>(request1, authHeaders), String.class);

        var request2 = createWoundedRequest("21-10006", "920920-1234567");
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/api/wounded", new HttpEntity<>(request2, authHeaders), String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
