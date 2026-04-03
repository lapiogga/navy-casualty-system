package com.navy.casualty.dead;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navy.casualty.dead.dto.DeadCreateRequest;
import com.navy.casualty.dead.dto.DeadDeleteRequest;
import com.navy.casualty.dead.dto.DeadResponse;
import com.navy.casualty.dead.service.DeadService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * DeadController 통합 테스트.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DeadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeadService deadService;

    private DeadCreateRequest createRequest(String serviceNumber, String ssn) {
        return new DeadCreateRequest(
                serviceNumber, "홍길동", ssn,
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
    @DisplayName("OPERATOR - 사망자 등록 -> 201")
    @WithMockUser(roles = "OPERATOR")
    void create_asOperator_returns201() throws Exception {
        mockMvc.perform(post("/api/dead")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createRequest("N-10001", "900101-1234567"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.serviceNumber").value("N-10001"))
                .andExpect(jsonPath("$.data.status").value("REGISTERED"));
    }

    @Test
    @DisplayName("OPERATOR - 군번 중복 등록 -> 409")
    @WithMockUser(roles = "OPERATOR")
    void create_duplicateServiceNumber_returns409() throws Exception {
        // 먼저 등록
        deadService.create(createRequest("N-20001", "900101-1111111"));

        // 같은 군번으로 재등록
        mockMvc.perform(post("/api/dead")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createRequest("N-20001", "900101-2222222"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 등록된 군번입니다"));
    }

    @Test
    @DisplayName("OPERATOR - 주민번호 중복 등록 -> 409 (DEAD-07)")
    @WithMockUser(roles = "OPERATOR")
    void create_duplicateSsn_returns409() throws Exception {
        deadService.create(createRequest("N-30001", "900101-3333333"));

        mockMvc.perform(post("/api/dead")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createRequest("N-30002", "900101-3333333"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 등록된 주민번호입니다"));
    }

    @Test
    @DisplayName("VIEWER - 사망자 검색 -> 200")
    @WithMockUser(roles = "VIEWER")
    void search_asViewer_returns200() throws Exception {
        mockMvc.perform(get("/api/dead")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("VIEWER - 사망자 등록 시도 -> 403")
    @WithMockUser(roles = "VIEWER")
    void create_asViewer_returns403() throws Exception {
        mockMvc.perform(post("/api/dead")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                createRequest("N-40001", "900101-4444444"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("MANAGER - 사망자 논리 삭제 -> 200")
    @WithMockUser(roles = "MANAGER")
    void delete_asManager_returns200() throws Exception {
        DeadResponse created = deadService.create(createRequest("N-50001", "900101-5555555"));

        mockMvc.perform(delete("/api/dead/" + created.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeadDeleteRequest("테스트 삭제 사유"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("MANAGER - 사망자 상태 변경 -> 200")
    @WithMockUser(roles = "MANAGER")
    void updateStatus_asManager_returns200() throws Exception {
        DeadResponse created = deadService.create(createRequest("N-60001", "900101-6666666"));

        mockMvc.perform(put("/api/dead/" + created.id() + "/status")
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
