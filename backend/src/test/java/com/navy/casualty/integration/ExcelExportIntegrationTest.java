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
 * Excel 다운로드 통합 테스트.
 * 사망자 Excel content-type 및 파일 크기를 검증한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExcelExportIntegrationTest {

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
    @DisplayName("사망자 Excel 다운로드 시 올바른 content-type과 파일 크기를 반환한다")
    void deadExcelDownload() {
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                "/api/dead/excel", HttpMethod.GET,
                new HttpEntity<>(authHeaders), byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType()).isNotNull();
        assertThat(resp.getHeaders().getContentType().toString())
                .contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().length).isGreaterThan(0);
    }

    @Test
    @DisplayName("상이자 Excel 다운로드 시 올바른 content-type을 반환한다")
    void woundedExcelDownload() {
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                "/api/wounded/excel", HttpMethod.GET,
                new HttpEntity<>(authHeaders), byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType()).isNotNull();
        assertThat(resp.getHeaders().getContentType().toString())
                .contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @Test
    @DisplayName("심사 Excel 다운로드 시 올바른 content-type을 반환한다")
    void reviewExcelDownload() {
        ResponseEntity<byte[]> resp = restTemplate.exchange(
                "/api/reviews/excel", HttpMethod.GET,
                new HttpEntity<>(authHeaders), byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getHeaders().getContentType()).isNotNull();
        assertThat(resp.getHeaders().getContentType().toString())
                .contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }
}
