package com.navy.casualty.document.dto;

import java.time.LocalDate;

import com.navy.casualty.document.enums.DocumentType;

/**
 * 문서 발급 이력 검색 요청 DTO. 모든 필드 nullable (동적 조건).
 */
public record DocumentIssueSearchRequest(
        DocumentType documentType,
        String issuedBy,
        LocalDate startDate,
        LocalDate endDate
) {
}
