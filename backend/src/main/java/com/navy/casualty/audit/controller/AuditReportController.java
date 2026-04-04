package com.navy.casualty.audit.controller;

import com.navy.casualty.audit.service.AuditReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 감사 보고서 REST API 컨트롤러.
 * 월별 감사 보고서를 PDF로 생성하여 반환한다.
 */
@RestController
@RequiredArgsConstructor
public class AuditReportController {

    private final AuditReportService auditReportService;

    /**
     * 월별 감사 보고서 PDF 생성.
     *
     * @param year  연도
     * @param month 월 (1~12)
     * @return PDF 바이트 배열
     */
    @GetMapping("/api/admin/audit-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> getMonthlyReport(
            @RequestParam int year, @RequestParam int month) {
        byte[] pdf = auditReportService.generateMonthlyReport(year, month);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"audit-report-" + year + "-" + month + ".pdf\"")
                .body(pdf);
    }
}
