package com.navy.casualty.document.entity;

import java.time.LocalDateTime;

import com.navy.casualty.document.enums.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 문서 발급 이력 엔티티.
 * TB_DOCUMENT_ISSUE 테이블에 매핑된다 (V7 마이그레이션).
 */
@Entity
@Table(name = "TB_DOCUMENT_ISSUE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DocumentIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "target_table", length = 50)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "issue_purpose", nullable = false, length = 500)
    private String issuePurpose;

    @Column(name = "issued_by", nullable = false, length = 50)
    private String issuedBy;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
}
