package com.navy.casualty.dead;

import java.io.ByteArrayInputStream;

import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.service.DeadExcelService;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DeadExcelService 테스트.
 * SXSSFWorkbook 생성, 헤더 행, HTTP 응답 헤더를 검증한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DeadExcelServiceTest {

    @Autowired
    private DeadExcelService deadExcelService;

    @Test
    @DisplayName("Excel 내보내기: Content-Type과 Content-Disposition 헤더가 올바르게 설정된다")
    @WithMockUser(roles = "VIEWER")
    void exportExcel_setsCorrectResponseHeaders() throws Exception {
        // given
        DeadSearchRequest request = new DeadSearchRequest(null, null, null, null, null, null, null, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        deadExcelService.exportExcel(request, response);

        // then
        assertThat(response.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertThat(response.getHeader("Content-Disposition"))
                .startsWith("attachment; filename=dead_list_")
                .endsWith(".xlsx");
    }

    @Test
    @DisplayName("Excel 내보내기: 헤더 행에 15개 컬럼이 포함된다")
    @WithMockUser(roles = "VIEWER")
    void exportExcel_containsHeaderRow() throws Exception {
        // given
        DeadSearchRequest request = new DeadSearchRequest(null, null, null, null, null, null, null, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        deadExcelService.exportExcel(request, response);

        // then
        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(response.getContentAsByteArray()))) {
            XSSFSheet sheet = workbook.getSheet("사망자 현황");
            assertThat(sheet).isNotNull();
            assertThat(sheet.getRow(0).getPhysicalNumberOfCells()).isEqualTo(15);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("번호");
            assertThat(sheet.getRow(0).getCell(3).getStringCellValue()).isEqualTo("성명");
            assertThat(sheet.getRow(0).getCell(4).getStringCellValue()).isEqualTo("주민등록번호");
            assertThat(sheet.getRow(0).getCell(14).getStringCellValue()).isEqualTo("상태");
        }
    }

    @Test
    @DisplayName("Excel 내보내기: 데이터가 없어도 정상적으로 빈 시트를 생성한다")
    @WithMockUser(roles = "OPERATOR")
    void exportExcel_emptyDataProducesValidExcel() throws Exception {
        // given
        DeadSearchRequest request = new DeadSearchRequest(null, null, null, null, null, null, null, null);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        deadExcelService.exportExcel(request, response);

        // then
        byte[] content = response.getContentAsByteArray();
        assertThat(content).isNotEmpty();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            XSSFSheet sheet = workbook.getSheet("사망자 현황");
            assertThat(sheet).isNotNull();
            // 헤더만 존재, 데이터 행 없음
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(1);
        }
    }
}
