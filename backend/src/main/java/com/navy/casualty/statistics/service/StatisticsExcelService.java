package com.navy.casualty.statistics.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.DeadRosterResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계 Excel 내보내기 서비스.
 * DeadExcelService 패턴 100% 복제: SXSSFWorkbook(100) + 헤더 스타일 + Content-Disposition.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsExcelService {

    private final StatisticsService statisticsService;

    /**
     * 신분별 통계 Excel 내보내기.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportBranchStatExcel(HttpServletResponse response) throws IOException {
        List<BranchStatResponse> data = statisticsService.getByBranch();
        String[] headers = {"신분", "인원수"};

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("신분별 사망자 현황");
            createHeaderRow(workbook, sheet, headers);

            long total = 0;
            for (int i = 0; i < data.size(); i++) {
                BranchStatResponse item = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.branchName());
                row.createCell(1).setCellValue(item.count());
                total += item.count();
            }

            // 합계 행
            Row totalRow = sheet.createRow(data.size() + 1);
            totalRow.createCell(0).setCellValue("합계");
            totalRow.createCell(1).setCellValue(total);

            writeResponse(response, workbook, "branch_stat");
        }
    }

    /**
     * 월별 통계 Excel 내보내기.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportMonthlyStatExcel(HttpServletResponse response) throws IOException {
        List<MonthlyStatResponse> data = statisticsService.getByMonth();
        String[] headers = {"연도", "월", "인원수"};

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("월별 사망자 현황");
            createHeaderRow(workbook, sheet, headers);

            for (int i = 0; i < data.size(); i++) {
                MonthlyStatResponse item = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.year());
                row.createCell(1).setCellValue(item.month());
                row.createCell(2).setCellValue(item.count());
            }

            writeResponse(response, workbook, "monthly_stat");
        }
    }

    /**
     * 연도별 통계 Excel 내보내기.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportYearlyStatExcel(HttpServletResponse response) throws IOException {
        List<YearlyStatResponse> data = statisticsService.getByYear();
        String[] headers = {"연도", "인원수"};

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("연도별 사망자 현황");
            createHeaderRow(workbook, sheet, headers);

            for (int i = 0; i < data.size(); i++) {
                YearlyStatResponse item = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.year());
                row.createCell(1).setCellValue(item.count());
            }

            writeResponse(response, workbook, "yearly_stat");
        }
    }

    /**
     * 부대별 통계 Excel 내보내기.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportUnitStatExcel(HttpServletResponse response) throws IOException {
        List<UnitStatResponse> data = statisticsService.getByUnit();
        String[] headers = {"부대", "인원수"};

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("부대별 사망자 현황");
            createHeaderRow(workbook, sheet, headers);

            long total = 0;
            for (int i = 0; i < data.size(); i++) {
                UnitStatResponse item = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.unitName());
                row.createCell(1).setCellValue(item.count());
                total += item.count();
            }

            Row totalRow = sheet.createRow(data.size() + 1);
            totalRow.createCell(0).setCellValue("합계");
            totalRow.createCell(1).setCellValue(total);

            writeResponse(response, workbook, "unit_stat");
        }
    }

    /**
     * 부대별 사망자 명부 Excel 내보내기.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportUnitRosterExcel(Long unitId, HttpServletResponse response) throws IOException {
        List<DeadRosterResponse> data = statisticsService.getRosterByUnit(unitId);
        writeRosterExcel(data, response, "unit_roster");
    }

    /**
     * 전사망자 명부 Excel 내보내기.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportAllRosterExcel(HttpServletResponse response) throws IOException {
        List<DeadRosterResponse> data = statisticsService.getRosterAll();
        writeRosterExcel(data, response, "all_roster");
    }

    /**
     * 명부 Excel 공통 작성 로직.
     */
    private void writeRosterExcel(List<DeadRosterResponse> data,
            HttpServletResponse response, String filePrefix) throws IOException {
        String[] headers = {"번호", "군구분", "군번", "성명", "주민등록번호",
                "계급", "소속", "사망일자", "사망구분", "상태"};

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("사망자 명부");
            createHeaderRow(workbook, sheet, headers);

            for (int i = 0; i < data.size(); i++) {
                DeadRosterResponse item = data.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(item.branchName());
                row.createCell(2).setCellValue(item.serviceNumber());
                row.createCell(3).setCellValue(item.name());
                row.createCell(4).setCellValue(item.ssnMasked());
                row.createCell(5).setCellValue(item.rankName());
                row.createCell(6).setCellValue(item.unitName());
                row.createCell(7).setCellValue(
                        item.deathDate() != null ? item.deathDate().toString() : "");
                row.createCell(8).setCellValue(item.deathTypeName());
                row.createCell(9).setCellValue(item.status());
            }

            writeResponse(response, workbook, filePrefix);
        }
    }

    /**
     * 헤더 행을 생성한다.
     */
    private void createHeaderRow(SXSSFWorkbook workbook, Sheet sheet, String[] headers) {
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * 헤더 셀 스타일을 생성한다 (굵은 글씨 + 회색 배경).
     */
    private CellStyle createHeaderStyle(SXSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * HTTP 응답에 Excel 파일을 쓴다.
     */
    private void writeResponse(HttpServletResponse response, SXSSFWorkbook workbook,
            String filePrefix) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=" + filePrefix + "_" + LocalDate.now() + ".xlsx");
        workbook.write(response.getOutputStream());
    }
}
