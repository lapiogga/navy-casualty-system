package com.navy.casualty.document.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navy.casualty.document.dto.DocumentIssueRequest;
import com.navy.casualty.document.entity.DocumentIssue;
import com.navy.casualty.document.enums.DocumentType;
import com.navy.casualty.document.service.DocumentGenerationService;
import com.navy.casualty.document.service.DocumentIssueService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * DocumentController 통합 테스트.
 * PDF 생성 API의 응답 형식, 인증, 인가를 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentGenerationService documentGenerationService;

    @MockitoBean
    private DocumentIssueService documentIssueService;

    private static final byte[] FAKE_PDF = "%PDF-1.4 fake content".getBytes();

    @Test
    @DisplayName("OPERATOR - POST /{type}/generate 호출 시 Content-Type이 application/pdf이다")
    @WithMockUser(roles = "OPERATOR")
    void test_generateDocument_returnsPdf() throws Exception {
        // given
        when(documentGenerationService.generate(eq(DocumentType.DEAD_CERTIFICATE), anyLong()))
                .thenReturn(FAKE_PDF);
        when(documentIssueService.recordIssue(any(), anyLong(), anyString()))
                .thenReturn(DocumentIssue.builder()
                        .documentType(DocumentType.DEAD_CERTIFICATE)
                        .targetId(1L)
                        .issuePurpose("테스트")
                        .issuedBy("user")
                        .build());

        // when & then
        mockMvc.perform(post("/api/documents/DEAD_CERTIFICATE/generate")
                        .param("targetId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DocumentIssueRequest("보훈 확인 목적"))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().exists("Content-Disposition"));
    }

    @Test
    @DisplayName("인증 없이 호출 시 401 반환")
    void test_generateDocument_unauthorized() throws Exception {
        mockMvc.perform(post("/api/documents/DEAD_CERTIFICATE/generate")
                        .param("targetId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DocumentIssueRequest("테스트"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("VIEWER 역할로 호출 시 403 반환 (OPERATOR 이상 필요)")
    @WithMockUser(roles = "VIEWER")
    void test_generateDocument_forbidden() throws Exception {
        mockMvc.perform(post("/api/documents/DEAD_CERTIFICATE/generate")
                        .param("targetId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DocumentIssueRequest("테스트"))))
                .andExpect(status().isForbidden());
    }
}
