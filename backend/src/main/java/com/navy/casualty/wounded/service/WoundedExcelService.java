package com.navy.casualty.wounded.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.code.entity.BranchCode;
import com.navy.casualty.code.entity.RankCode;
import com.navy.casualty.code.entity.UnitCode;
import com.navy.casualty.code.entity.VeteransOffice;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.wounded.dto.WoundedSearchRequest;
import com.navy.casualty.wounded.entity.Wounded;
import com.navy.casualty.wounded.repository.WoundedRepository;

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
 * 상이자 Excel 내보내기 서비스.
 * SXSSFWorkbook(메모리 윈도우 100행)으로 대용량 데이터도 안정적으로 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WoundedExcelService {

    private final WoundedRepository woundedRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final VeteransOfficeRepository veteransOfficeRepository;

    private static final String[] HEADERS = {
            "번호", "군구분", "군번", "성명", "주민등록번호", "생년월일",
            "계급", "소속", "입대일자", "전화번호", "주소",
            "보훈청명", "병명", "상이구분", "상태"
    };

    /**
     * 검색 조건이 적용된 상이자 현황을 Excel 파일로 내보낸다.
     * 주민번호는 사용자 역할에 따라 마스킹 적용.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_WOUNDED")
    public void exportExcel(WoundedSearchRequest request, HttpServletResponse response) throws IOException {
        String userRole = getCurrentUserRole();
        List<Wounded> list = woundedRepository.searchAll(request);

        // 코드 테이블을 Map으로 캐시 (DB 조회 최소화)
        Map<Long, String> rankMap = rankCodeRepository.findAll().stream()
                .collect(Collectors.toMap(RankCode::getId, RankCode::getRankName));
        Map<Long, String> branchMap = branchCodeRepository.findAll().stream()
                .collect(Collectors.toMap(BranchCode::getId, BranchCode::getBranchName));
        Map<Long, String> unitMap = unitCodeRepository.findAll().stream()
                .collect(Collectors.toMap(UnitCode::getId, UnitCode::getUnitName));
        Map<Long, String> officeMap = veteransOfficeRepository.findAll().stream()
                .collect(Collectors.toMap(VeteransOffice::getId, VeteransOffice::getOfficeName));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("상이자 현황");

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
                Wounded w = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(
                        w.getBranchId() != null ? branchMap.getOrDefault(w.getBranchId(), "") : "");
                row.createCell(2).setCellValue(w.getServiceNumber());
                row.createCell(3).setCellValue(w.getName());
                row.createCell(4).setCellValue(RrnMaskingUtil.mask(w.getSsnEncrypted(), userRole));
                row.createCell(5).setCellValue(
                        w.getBirthDate() != null ? w.getBirthDate().toString() : "");
                row.createCell(6).setCellValue(
                        w.getRankId() != null ? rankMap.getOrDefault(w.getRankId(), "") : "");
                row.createCell(7).setCellValue(
                        w.getUnitId() != null ? unitMap.getOrDefault(w.getUnitId(), "") : "");
                row.createCell(8).setCellValue(
                        w.getEnlistmentDate() != null ? w.getEnlistmentDate().toString() : "");
                row.createCell(9).setCellValue(w.getPhone() != null ? w.getPhone() : "");
                row.createCell(10).setCellValue(w.getAddress() != null ? w.getAddress() : "");
                row.createCell(11).setCellValue(
                        w.getVeteransOfficeId() != null ? officeMap.getOrDefault(w.getVeteransOfficeId(), "") : "");
                row.createCell(12).setCellValue(w.getDiseaseName() != null ? w.getDiseaseName() : "");
                row.createCell(13).setCellValue(w.getWoundType() != null ? w.getWoundType().getLabel() : "");
                row.createCell(14).setCellValue(w.getStatus().name());
            }

            // HTTP 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=wounded_list_" + LocalDate.now() + ".xlsx");
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
