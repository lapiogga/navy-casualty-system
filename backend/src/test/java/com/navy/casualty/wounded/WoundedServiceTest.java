package com.navy.casualty.wounded;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import com.navy.casualty.wounded.dto.WoundedCreateRequest;
import com.navy.casualty.wounded.dto.WoundedResponse;
import com.navy.casualty.wounded.entity.WoundType;
import com.navy.casualty.wounded.entity.WoundedStatus;
import com.navy.casualty.wounded.service.WoundedService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * WoundedService 단위(통합) 테스트.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WoundedServiceTest {

    @Autowired
    private WoundedService woundedService;

    @Autowired
    private EntityManager entityManager;

    private WoundedCreateRequest createRequest(String serviceNumber, String ssn) {
        return new WoundedCreateRequest(
                serviceNumber,
                "김상이",
                ssn,
                LocalDate.of(1990, 5, 15),
                null, null, null,
                LocalDate.of(2010, 3, 1),
                "010-9876-5432",
                "서울시 종로구",
                null,
                "좌측 슬관절 손상",
                WoundType.COMBAT_WOUND
        );
    }

    @Test
    @DisplayName("상이자 등록 성공 - status=REGISTERED")
    @WithMockUser(roles = "OPERATOR")
    void create_success() {
        WoundedResponse response = woundedService.create(createRequest("W-12345", "900515-1234567"));

        assertThat(response).isNotNull();
        assertThat(response.serviceNumber()).isEqualTo("W-12345");
        assertThat(response.status()).isEqualTo("REGISTERED");
        assertThat(response.woundTypeName()).isEqualTo("전공상");
    }

    @Test
    @DisplayName("군번 중복 시 IllegalArgumentException")
    @WithMockUser(roles = "OPERATOR")
    void create_duplicateServiceNumber_throws() {
        woundedService.create(createRequest("W-99999", "900515-1111111"));

        assertThatThrownBy(() -> woundedService.create(createRequest("W-99999", "900515-2222222")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 군번입니다");
    }

    @Test
    @DisplayName("주민번호 해시 중복 시 IllegalArgumentException")
    @WithMockUser(roles = "OPERATOR")
    void create_duplicateSsnHash_throws() {
        woundedService.create(createRequest("W-00001", "900515-3333333"));

        assertThatThrownBy(() -> woundedService.create(createRequest("W-00002", "900515-3333333")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 주민번호입니다");
    }

    @Test
    @DisplayName("상태 전이: REGISTERED -> UNDER_REVIEW -> CONFIRMED -> NOTIFIED 순서")
    @WithMockUser(roles = "MANAGER")
    void updateStatus_fullTransition() {
        WoundedResponse response = woundedService.create(createRequest("W-44444", "900515-4444444"));
        Long id = response.id();

        woundedService.updateStatus(id, WoundedStatus.UNDER_REVIEW);
        woundedService.updateStatus(id, WoundedStatus.CONFIRMED);
        woundedService.updateStatus(id, WoundedStatus.NOTIFIED);

        // 완료 후 추가 전이 불가
        assertThatThrownBy(() -> woundedService.updateStatus(id, WoundedStatus.REGISTERED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("허용되지 않은 상태 전이 시 IllegalStateException")
    @WithMockUser(roles = "MANAGER")
    void updateStatus_invalidTransition_throws() {
        WoundedResponse response = woundedService.create(createRequest("W-55555", "900515-5555555"));

        // REGISTERED -> CONFIRMED 건너뛰기 불가
        assertThatThrownBy(() -> woundedService.updateStatus(response.id(), WoundedStatus.CONFIRMED))
                .isInstanceOf(IllegalStateException.class);
    }
}
