package com.navy.casualty.statistics.controller;

import java.io.IOException;
import java.util.List;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.DeadRosterResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;
import com.navy.casualty.statistics.service.StatisticsExcelService;
import com.navy.casualty.statistics.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통계 REST API 컨트롤러.
 * 4종 집계 + 2종 명부 조회 API, 각각의 Excel 다운로드 엔드포인트를 제공한다.
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final StatisticsExcelService statisticsExcelService;

    // === 통계 집계 API ===

    @GetMapping("/branch")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "SELECT", targetTable = "TB_DEAD")
    public ApiResponse<List<BranchStatResponse>> getByBranch() {
        return ApiResponse.ok(statisticsService.getByBranch());
    }

    @GetMapping("/monthly")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "SELECT", targetTable = "TB_DEAD")
    public ApiResponse<List<MonthlyStatResponse>> getByMonth() {
        return ApiResponse.ok(statisticsService.getByMonth());
    }

    @GetMapping("/yearly")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "SELECT", targetTable = "TB_DEAD")
    public ApiResponse<List<YearlyStatResponse>> getByYear() {
        return ApiResponse.ok(statisticsService.getByYear());
    }

    @GetMapping("/unit")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "SELECT", targetTable = "TB_DEAD")
    public ApiResponse<List<UnitStatResponse>> getByUnit() {
        return ApiResponse.ok(statisticsService.getByUnit());
    }

    // === 명부 API ===

    @GetMapping("/roster/unit")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "SELECT", targetTable = "TB_DEAD")
    public ApiResponse<List<DeadRosterResponse>> getRosterByUnit(@RequestParam Long unitId) {
        return ApiResponse.ok(statisticsService.getRosterByUnit(unitId));
    }

    @GetMapping("/roster/all")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "SELECT", targetTable = "TB_DEAD")
    public ApiResponse<List<DeadRosterResponse>> getRosterAll() {
        return ApiResponse.ok(statisticsService.getRosterAll());
    }

    // === Excel 다운로드 API ===

    @GetMapping("/branch/excel")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportBranchExcel(HttpServletResponse response) throws IOException {
        statisticsExcelService.exportBranchStatExcel(response);
    }

    @GetMapping("/monthly/excel")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportMonthlyExcel(HttpServletResponse response) throws IOException {
        statisticsExcelService.exportMonthlyStatExcel(response);
    }

    @GetMapping("/yearly/excel")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportYearlyExcel(HttpServletResponse response) throws IOException {
        statisticsExcelService.exportYearlyStatExcel(response);
    }

    @GetMapping("/unit/excel")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportUnitExcel(HttpServletResponse response) throws IOException {
        statisticsExcelService.exportUnitStatExcel(response);
    }

    @GetMapping("/roster/unit/excel")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportUnitRosterExcel(@RequestParam Long unitId,
            HttpServletResponse response) throws IOException {
        statisticsExcelService.exportUnitRosterExcel(unitId, response);
    }

    @GetMapping("/roster/all/excel")
    @PreAuthorize("hasRole('VIEWER')")
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportAllRosterExcel(HttpServletResponse response) throws IOException {
        statisticsExcelService.exportAllRosterExcel(response);
    }
}
