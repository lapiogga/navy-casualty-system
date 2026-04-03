package com.navy.casualty.document.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.navy.casualty.document.entity.DocumentIssue;
import com.navy.casualty.document.enums.DocumentType;
import com.navy.casualty.document.repository.DocumentIssueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * DocumentIssueService 통합 테스트.
 * 발급 이력 기록 및 검색을 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentIssueServiceTest {

    @Autowired
    private DocumentIssueService documentIssueService;

    @Autowired
    private DocumentIssueRepository documentIssueRepository;

    @Test
    @DisplayName("recordIssue() 호출 시 TB_DOCUMENT_ISSUE에 레코드가 저장된다")
    @WithMockUser(username = "testuser", roles = "OPERATOR")
    void test_recordIssue() {
        // given
        long beforeCount = documentIssueRepository.count();

        // when
        documentIssueService.recordIssue(DocumentType.DEAD_CERTIFICATE, 1L, "보훈 확인용");

        // then
        long afterCount = documentIssueRepository.count();
        assertThat(afterCount).isEqualTo(beforeCount + 1);
    }

    @Test
    @DisplayName("issuedBy 필드에 현재 인증 사용자명이 기록된다")
    @WithMockUser(username = "operator01", roles = "OPERATOR")
    void test_recordIssue_setsIssuedBy() {
        // when
        DocumentIssue saved = documentIssueService.recordIssue(
                DocumentType.WOUNDED_CERTIFICATE, 2L, "국가유공자 확인용");

        // then
        assertThat(saved.getIssuedBy()).isEqualTo("operator01");
    }

    @Test
    @DisplayName("issuedAt 필드에 현재 시각이 기록된다")
    @WithMockUser(username = "testuser", roles = "OPERATOR")
    void test_recordIssue_setsIssuedAt() {
        // when
        DocumentIssue saved = documentIssueService.recordIssue(
                DocumentType.REVIEW_RESULT, 3L, "심사결과 발급");

        // then
        assertThat(saved.getIssuedAt()).isNotNull();
    }

    @Test
    @DisplayName("search() 호출 시 documentType 필터가 동작한다")
    @WithMockUser(username = "testuser", roles = "OPERATOR")
    void test_search_filtersByDocumentType() {
        // given: 서로 다른 문서 유형 2건 저장
        documentIssueService.recordIssue(DocumentType.DEAD_CERTIFICATE, 1L, "사망자 확인");
        documentIssueService.recordIssue(DocumentType.WOUNDED_CERTIFICATE, 2L, "상이자 확인");
        documentIssueService.recordIssue(DocumentType.DEAD_CERTIFICATE, 3L, "사망자 확인2");

        // when: DEAD_CERTIFICATE만 검색
        var request = new com.navy.casualty.document.dto.DocumentIssueSearchRequest(
                DocumentType.DEAD_CERTIFICATE, null, null, null);
        var page = documentIssueService.search(request,
                org.springframework.data.domain.PageRequest.of(0, 10));

        // then
        assertThat(page.getContent()).allSatisfy(response ->
                assertThat(response.documentType()).isEqualTo("DEAD_CERTIFICATE"));
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(2);
    }
}
