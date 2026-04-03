package com.navy.casualty.dead;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import com.navy.casualty.dead.dto.DeadCreateRequest;
import com.navy.casualty.dead.dto.DeadResponse;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.entity.DeadStatus;
import com.navy.casualty.dead.repository.DeadRepository;
import com.navy.casualty.dead.service.DeadService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * DeadService 단위(통합) 테스트.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeadServiceTest {

    @Autowired
    private DeadService deadService;

    @Autowired
    private DeadRepository deadRepository;

    @Autowired
    private EntityManager entityManager;

    private DeadCreateRequest createRequest(String serviceNumber, String ssn) {
        return new DeadCreateRequest(
                serviceNumber,
                "홍길동",
                ssn,
                LocalDate.of(1990, 1, 1),
                null, null, null,
                LocalDate.of(2010, 3, 1),
                "010-1234-5678",
                null, null,
                "서울시 강남구",
                LocalDate.of(2025, 12, 1)
        );
    }

    @Test
    @DisplayName("사망자 등록 성공 - status=REGISTERED")
    @WithMockUser(roles = "OPERATOR")
    void create_success() {
        DeadResponse response = deadService.create(createRequest("N-12345", "900101-1234567"));

        assertThat(response).isNotNull();
        assertThat(response.serviceNumber()).isEqualTo("N-12345");
        assertThat(response.status()).isEqualTo("REGISTERED");
    }

    @Test
    @DisplayName("군번 중복 시 IllegalArgumentException")
    @WithMockUser(roles = "OPERATOR")
    void create_duplicateServiceNumber_throws() {
        deadService.create(createRequest("N-99999", "900101-1111111"));

        assertThatThrownBy(() -> deadService.create(createRequest("N-99999", "900101-2222222")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 군번입니다");
    }

    @Test
    @DisplayName("주민번호 해시 중복 시 IllegalArgumentException (DEAD-07)")
    @WithMockUser(roles = "OPERATOR")
    void create_duplicateSsnHash_throws() {
        deadService.create(createRequest("N-00001", "900101-3333333"));

        assertThatThrownBy(() -> deadService.create(createRequest("N-00002", "900101-3333333")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 주민번호입니다");
    }

    @Test
    @DisplayName("논리 삭제 후 isDeleted=true")
    @WithMockUser(roles = "MANAGER")
    void softDelete_marksAsDeleted() {
        DeadResponse response = deadService.create(createRequest("N-77777", "900101-7777777"));

        deadService.softDelete(response.id(), "테스트 삭제 사유");

        // flush 후 영속성 컨텍스트를 클리어하여 SQLRestriction 무시 없이 직접 조회
        entityManager.flush();

        // SQLRestriction으로 논리 삭제된 레코드는 JPA findById에서 조회 불가
        // native query로 직접 확인
        Dead deleted = (Dead) entityManager
                .createNativeQuery("SELECT * FROM TB_DEAD WHERE id = :id", Dead.class)
                .setParameter("id", response.id())
                .getSingleResult();

        assertThat(deleted).isNotNull();
        assertThat(deleted.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("허용되지 않은 상태 전이 시 IllegalStateException")
    @WithMockUser(roles = "MANAGER")
    void updateStatus_invalidTransition_throws() {
        DeadResponse response = deadService.create(createRequest("N-55555", "900101-5555555"));

        // REGISTERED -> NOTIFIED는 불가
        assertThatThrownBy(() -> deadService.updateStatus(response.id(), DeadStatus.NOTIFIED))
                .isInstanceOf(IllegalStateException.class);
    }
}
