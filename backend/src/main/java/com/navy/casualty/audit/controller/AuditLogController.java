package com.navy.casualty.audit.controller;

import com.navy.casualty.audit.dto.AuditLogResponse;
import com.navy.casualty.audit.dto.AuditLogSearchRequest;
import com.navy.casualty.audit.service.AuditLogService;
import com.navy.casualty.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ADMIN 전용 감사 로그 검색 컨트롤러.
 * append-only 원칙에 따라 조회만 제공한다 (수정/삭제 엔드포인트 없음).
 */
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * D-07 필터 기반 감사 로그를 검색한다.
     * 기본 정렬: 생성일시 내림차순 (최신순).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> searchAuditLogs(
            @ModelAttribute AuditLogSearchRequest search,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditLogResponse> result = auditLogService.search(
                search.startDate() != null ? search.startDate().atStartOfDay() : null,
                search.endDate() != null ? search.endDate().plusDays(1).atStartOfDay() : null,
                search.userId(),
                search.action(),
                pageable
        ).map(AuditLogResponse::from);

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
