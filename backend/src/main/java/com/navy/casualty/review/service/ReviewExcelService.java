package com.navy.casualty.review.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.code.entity.BranchCode;
import com.navy.casualty.code.entity.RankCode;
import com.navy.casualty.code.entity.UnitCode;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.review.dto.ReviewSearchRequest;
import com.navy.casualty.review.entity.Review;
import com.navy.casualty.review.repository.ReviewRepository;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전공사상심사 Excel 내보내기 서비스.
 * SXSSFWorkbook(메모리 윈도우 100행)으로 대용량 데이터도 안정적으로 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewExcelService {

    private final ReviewRepository reviewRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final UnitCodeRepository unitCodeRepository;

    private static final String[] HEADERS = {
            "번호", "심사차수", "심사일자", "군구분", "군번", "성명",
            "주민등록번호", "생년월일", "계급", "소속", "입대일자",
            "병명", "소속부대 심사결과", "분류", "상태", "보훈청 통보일"
    };

    /**
     * 검색 조건이 적용된 전공사상심사 현황을 Excel 파일로 내보낸다.
     * 주민번호는 사용자 역할에 따라 마스킹 적용.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_REVIEW")
    public void exportExcel(ReviewSearchRequest request, HttpServletResponse response) throws IOException {
        String userRole = getCurrentUserRole();
        List<Review> list = reviewRepository.searchAll(request);

        // 코드 테이블을 Map으로 캐시 (DB 조회 최소화)
        Map<Long, String> rankMap = rankCodeRepository.findAll().stream()
                .collect(Collectors.toMap(RankCode::getId, RankCode::getRankName));
        Map<Long, String> branchMap = branchCodeRepository.findAll().stream()
                .collect(Collectors.toMap(BranchCode::getId, BranchCode::getBranchName));
        Map<Long, String> unitMap = unitCodeRepository.findAll().stream()
                .collect(Collectors.toMap(UnitCode::getId, UnitCode::getUnitName));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("전공사상심사 현황");

            // 헤더 행 생성
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }

            // 데이터 행 생성
            for (int i = 0; i < list.size(); i++) {
                Review r = list.get(i);
                Row row = sheet.createRow(i + 1);
                int col = 0;
                row.createCell(col++).setCellValue(i + 1);
                row.createCell(col++).setCellValue(r.getReviewRound());
                row.createCell(col++).setCellValue(
                        r.getReviewDate() != null ? r.getReviewDate().toString() : "");
                row.createCell(col++).setCellValue(
                        r.getBranchId() != null ? branchMap.getOrDefault(r.getBranchId(), "") : "");
                row.createCell(col++).setCellValue(r.getServiceNumber());
                row.createCell(col++).setCellValue(r.getName());
                row.createCell(col++).setCellValue(RrnMaskingUtil.mask(r.getSsnEncrypted(), userRole));
                row.createCell(col++).setCellValue(
                        r.getBirthDate() != null ? r.getBirthDate().toString() : "");
                row.createCell(col++).setCellValue(
                        r.getRankId() != null ? rankMap.getOrDefault(r.getRankId(), "") : "");
                row.createCell(col++).setCellValue(
                        r.getUnitId() != null ? unitMap.getOrDefault(r.getUnitId(), "") : "");
                row.createCell(col++).setCellValue(
                        r.getEnlistmentDate() != null ? r.getEnlistmentDate().toString() : "");
                row.createCell(col++).setCellValue(r.getDiseaseName() != null ? r.getDiseaseName() : "");
                row.createCell(col++).setCellValue(
                        r.getUnitReviewResult() != null ? r.getUnitReviewResult() : "");
                row.createCell(col++).setCellValue(
                        r.getClassification() != null ? r.getClassification().getLabel() : "");
                row.createCell(col++).setCellValue(r.getStatus().name());
                row.createCell(col++).setCellValue(
                        r.getNotificationDate() != null ? r.getNotificationDate().toString() : "");
            }

            // HTTP 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=review_list_" + LocalDate.now() + ".xlsx");
            workbook.write(response.getOutputStream());
        }
    }

    /**
     * 현재 인증된 사용자의 역할을 추출한다.
     */
    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().findFirst()
                .map(GrantedAuthority::getAuthority).orElse("ROLE_VIEWER");
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
}
