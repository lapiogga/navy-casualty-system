package com.navy.casualty.audit.dto;

import java.time.LocalDateTime;

import com.navy.casualty.audit.entity.AuditLogEntry;

/**
 * 감사 로그 응답 DTO.
 */
public record AuditLogResponse(
        Long id,
        String userId,
        String action,
        String targetTable,
        Long targetId,
        String detail,
        String ipAddress,
        LocalDateTime createdAt
) {

    /**
     * AuditLogEntry 엔티티로부터 응답 DTO를 생성한다.
     */
    public static AuditLogResponse from(AuditLogEntry entry) {
        return new AuditLogResponse(
                entry.getId(),
                entry.getUserId(),
                entry.getAction(),
                entry.getTargetTable(),
                entry.getTargetId(),
                entry.getDetail(),
                entry.getIpAddress(),
                entry.getCreatedAt()
        );
    }
}
