package com.navy.casualty.audit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.navy.casualty.audit.entity.AuditLogEntry;
import com.navy.casualty.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuditLogController 통합 테스트.
 * ADMIN 전용 감사 로그 검색 API 검증.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.save(AuditLogEntry.builder()
                .userId("admin")
                .action("CREATE")
                .targetTable("TB_USER")
                .targetId(1L)
                .detail("사용자 생성")
                .ipAddress("127.0.0.1")
                .build());

        auditLogRepository.save(AuditLogEntry.builder()
                .userId("admin")
                .action("UPDATE")
                .targetTable("TB_USER")
                .targetId(2L)
                .detail("역할 변경")
                .ipAddress("127.0.0.1")
                .build());
    }

    @Test
    @DisplayName("ADMIN - 감사 로그 전체 조회 -> 200")
    @WithMockUser(roles = "ADMIN")
    void searchAuditLogs_asAdmin_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("ADMIN - 기간 필터 조회 -> 200")
    @WithMockUser(roles = "ADMIN")
    void searchAuditLogs_withDateFilter_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("ADMIN - 작업유형 필터 조회 -> 200")
    @WithMockUser(roles = "ADMIN")
    void searchAuditLogs_withActionFilter_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("action", "CREATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("ADMIN - 사용자ID 필터 조회 -> 200")
    @WithMockUser(roles = "ADMIN")
    void searchAuditLogs_withUserIdFilter_returns200() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("userId", "admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("NON-ADMIN(OPERATOR) - 감사 로그 접근 -> 403")
    @WithMockUser(roles = "OPERATOR")
    void searchAuditLogs_asOperator_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }
}
