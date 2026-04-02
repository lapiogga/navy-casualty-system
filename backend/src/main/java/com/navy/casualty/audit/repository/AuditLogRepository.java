package com.navy.casualty.audit.repository;

import java.time.LocalDateTime;

import com.navy.casualty.audit.entity.AuditLogEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 감사 로그 리포지토리.
 * append-only 테이블이므로 save/find만 사용한다.
 */
public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long>,
        JpaSpecificationExecutor<AuditLogEntry> {

    Page<AuditLogEntry> findByCreatedAtBetween(
            LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<AuditLogEntry> findByUserId(String userId, Pageable pageable);
}
