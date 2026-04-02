package com.navy.casualty.audit;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.audit.aspect.AuditLogAspect;
import com.navy.casualty.audit.entity.AuditLogEntry;
import com.navy.casualty.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * AuditLogAspect 통합 테스트.
 * @AuditLog 어노테이션이 붙은 메서드 호출 시 감사 로그가 자동 기록되는지 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditLogAspectTest {

    @Autowired
    private TestAuditService testAuditService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @TestConfiguration
    @EnableAspectJAutoProxy
    static class Config {

        @Bean
        TestAuditService testAuditService() {
            return new TestAuditService();
        }

        @Bean
        AuditLogAspect auditLogAspect(AuditLogRepository repository) {
            return new AuditLogAspect(repository);
        }
    }

    /**
     * 테스트용 서비스. @AuditLog가 붙은 메서드를 제공한다.
     */
    @Service
    static class TestAuditService {

        @AuditLog(action = "CREATE", targetTable = "TB_DEAD")
        public Long createRecord(Long id) {
            return id;
        }

        @AuditLog(action = "VIEW", targetTable = "TB_DEAD")
        public String viewRecord(Long id) {
            return "조회 결과";
        }

        @AuditLog(action = "DELETE", targetTable = "TB_DEAD")
        public void failingMethod(Long id) {
            throw new RuntimeException("의도적 테스트 오류");
        }
    }

    @Test
    @DisplayName("@AuditLog 메서드 호출 시 감사 로그가 자동 기록된다")
    void auditLogIsRecordedOnMethodCall() {
        testAuditService.createRecord(1L);

        List<AuditLogEntry> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        AuditLogEntry entry = logs.get(logs.size() - 1);
        assertThat(entry.getAction()).isEqualTo("CREATE");
        assertThat(entry.getTargetTable()).isEqualTo("TB_DEAD");
        assertThat(entry.getUserId()).isNotNull();
    }

    @Test
    @DisplayName("VIEW 액션도 감사 로그에 기록된다")
    void viewActionIsLogged() {
        testAuditService.viewRecord(42L);

        List<AuditLogEntry> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        AuditLogEntry entry = logs.get(logs.size() - 1);
        assertThat(entry.getAction()).isEqualTo("VIEW");
        assertThat(entry.getTargetId()).isEqualTo(42L);
    }

    @Test
    @DisplayName("메서드 실패 시에도 감사 로그가 기록되고 예외는 재던진다")
    void auditLogIsRecordedEvenOnFailure() {
        assertThatThrownBy(() -> testAuditService.failingMethod(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("의도적 테스트 오류");

        List<AuditLogEntry> logs = auditLogRepository.findAll();
        assertThat(logs).isNotEmpty();

        AuditLogEntry entry = logs.get(logs.size() - 1);
        assertThat(entry.getAction()).isEqualTo("DELETE");
        assertThat(entry.getDetail()).contains("실패");
    }
}
