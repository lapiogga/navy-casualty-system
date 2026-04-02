package com.navy.casualty.audit.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 감사 로그 엔티티.
 * TB_AUDIT_LOG 테이블에 매핑되며 append-only로 운영된다.
 * setter 없이 Builder 패턴으로만 생성한다.
 */
@Entity
@Table(name = "TB_AUDIT_LOG")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "action", nullable = false, length = 20)
    private String action;

    @Column(name = "target_table", length = 50)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    private AuditLogEntry(String userId, String action, String targetTable,
                          Long targetId, String detail, String ipAddress) {
        this.userId = userId;
        this.action = action;
        this.targetTable = targetTable;
        this.targetId = targetId;
        this.detail = detail;
        this.ipAddress = ipAddress;
    }
}
