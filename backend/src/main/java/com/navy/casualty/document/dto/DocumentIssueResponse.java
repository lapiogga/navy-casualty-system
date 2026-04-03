package com.navy.casualty.document.dto;

import java.time.LocalDateTime;

import com.navy.casualty.document.entity.DocumentIssue;

/**
 * 문서 발급 이력 응답 DTO.
 */
public record DocumentIssueResponse(
        Long id,
        String documentType,
        String documentTypeName,
        String targetTable,
        Long targetId,
        String issuePurpose,
        String issuedBy,
        LocalDateTime issuedAt
) {

    /**
     * DocumentIssue 엔티티로부터 응답 DTO를 생성한다.
     */
    public static DocumentIssueResponse from(DocumentIssue issue) {
        return new DocumentIssueResponse(
                issue.getId(),
                issue.getDocumentType().name(),
                issue.getDocumentType().getFileName(),
                issue.getTargetTable(),
                issue.getTargetId(),
                issue.getIssuePurpose(),
                issue.getIssuedBy(),
                issue.getIssuedAt()
        );
    }
}
