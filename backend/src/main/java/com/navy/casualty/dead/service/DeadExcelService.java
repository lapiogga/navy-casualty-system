package com.navy.casualty.dead.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.code.entity.BranchCode;
import com.navy.casualty.code.entity.DeathCode;
import com.navy.casualty.code.entity.DeathType;
import com.navy.casualty.code.entity.RankCode;
import com.navy.casualty.code.entity.UnitCode;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.repository.DeadRepository;

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
 * 사망자 Excel 내보내기 서비스.
 * SXSSFWorkbook(메모리 윈도우 100행)으로 대용량 데이터도 안정적으로 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadExcelService {

    private final DeadRepository deadRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final DeathTypeRepository deathTypeRepository;
    private final DeathCodeRepository deathCodeRepository;
    private final UnitCodeRepository unitCodeRepository;

    private static final String[] HEADERS = {
            "번호", "군구분", "군번", "성명", "주민등록번호", "생년월일",
            "계급", "소속", "입대일자", "전화번호", "사망구분", "사망코드",
            "주소", "사망일자", "상태"
    };

    /**
     * 검색 조건이 적용된 사망자 현황을 Excel 파일로 내보낸다.
     * 주민번호는 사용자 역할에 따라 마스킹 적용.
     */
    @AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
    public void exportExcel(DeadSearchRequest request, HttpServletResponse response) throws IOException {
        String userRole = getCurrentUserRole();
        List<Dead> list = deadRepository.searchAll(request);

        // 코드 테이블을 Map으로 캐시 (DB 조회 최소화)
        Map<Long, String> rankMap = rankCodeRepository.findAll().stream()
                .collect(Collectors.toMap(RankCode::getId, RankCode::getRankName));
        Map<Long, String> branchMap = branchCodeRepository.findAll().stream()
                .collect(Collectors.toMap(BranchCode::getId, BranchCode::getBranchName));
        Map<Long, String> deathTypeMap = deathTypeRepository.findAll().stream()
                .collect(Collectors.toMap(DeathType::getId, DeathType::getTypeName));
        Map<Long, String> deathCodeMap = deathCodeRepository.findAll().stream()
                .collect(Collectors.toMap(DeathCode::getId, DeathCode::getCodeSymbol));
        Map<Long, String> unitMap = unitCodeRepository.findAll().stream()
                .collect(Collectors.toMap(UnitCode::getId, UnitCode::getUnitName));

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) {
            Sheet sheet = workbook.createSheet("사망자 현황");

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
                Dead dead = list.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(
                        dead.getBranchId() != null ? branchMap.getOrDefault(dead.getBranchId(), "") : "");
                row.createCell(2).setCellValue(dead.getServiceNumber());
                row.createCell(3).setCellValue(dead.getName());
                row.createCell(4).setCellValue(RrnMaskingUtil.mask(dead.getSsnEncrypted(), userRole));
                row.createCell(5).setCellValue(
                        dead.getBirthDate() != null ? dead.getBirthDate().toString() : "");
                row.createCell(6).setCellValue(
                        dead.getRankId() != null ? rankMap.getOrDefault(dead.getRankId(), "") : "");
                row.createCell(7).setCellValue(
                        dead.getUnitId() != null ? unitMap.getOrDefault(dead.getUnitId(), "") : "");
                row.createCell(8).setCellValue(
                        dead.getEnlistmentDate() != null ? dead.getEnlistmentDate().toString() : "");
                row.createCell(9).setCellValue(dead.getPhone() != null ? dead.getPhone() : "");
                row.createCell(10).setCellValue(
                        dead.getDeathTypeId() != null ? deathTypeMap.getOrDefault(dead.getDeathTypeId(), "") : "");
                row.createCell(11).setCellValue(
                        dead.getDeathCodeId() != null ? deathCodeMap.getOrDefault(dead.getDeathCodeId(), "") : "");
                row.createCell(12).setCellValue(dead.getAddress() != null ? dead.getAddress() : "");
                row.createCell(13).setCellValue(
                        dead.getDeathDate() != null ? dead.getDeathDate().toString() : "");
                row.createCell(14).setCellValue(dead.getStatus().name());
            }

            // HTTP 응답 설정
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition",
                    "attachment; filename=dead_list_" + LocalDate.now() + ".xlsx");
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
