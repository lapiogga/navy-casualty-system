package com.navy.casualty.audit.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * 감사 로그 검색 요청 DTO.
 * D-07 필터: 기간(startDate~endDate), 사용자ID, 작업유형.
 */
public record AuditLogSearchRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
        String userId,
        String action
) {
}
