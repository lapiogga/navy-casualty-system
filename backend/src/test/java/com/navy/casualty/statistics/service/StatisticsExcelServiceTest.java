package com.navy.casualty.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.DeadRosterResponse;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * StatisticsExcelService 단위 테스트.
 */
@ExtendWith(MockitoExtension.class)
class StatisticsExcelServiceTest {

    @Mock
    private StatisticsService statisticsService;

    @InjectMocks
    private StatisticsExcelService statisticsExcelService;

    @Test
    @DisplayName("신분별 통계 Excel 생성 - Content-Type 및 바이트 검증")
    void exportBranchStatExcel_createsValidXlsx() throws Exception {
        // given
        when(statisticsService.getByBranch()).thenReturn(List.of(
                new BranchStatResponse("육군", 10L),
                new BranchStatResponse("해군", 5L)
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        statisticsExcelService.exportBranchStatExcel(response);

        // then
        assertThat(response.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        byte[] content = response.getContentAsByteArray();
        assertThat(content.length).isGreaterThan(0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            XSSFSheet sheet = workbook.getSheet("신분별 사망자 현황");
            assertThat(sheet).isNotNull();
            // 헤더 + 데이터 2건 + 합계 1건 = 4행
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(4);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("신분");
            assertThat(sheet.getRow(0).getCell(1).getStringCellValue()).isEqualTo("인원수");
        }
    }

    @Test
    @DisplayName("부대별 명부 Excel - 마스킹된 주민번호 포함 확인")
    void exportUnitRosterExcel_includesMaskedSsn() throws Exception {
        // given
        when(statisticsService.getRosterByUnit(1L)).thenReturn(List.of(
                new DeadRosterResponse(1L, "해군", "N-12345", "홍길동",
                        "******-*******", "상병", "1함대",
                        LocalDate.of(2025, 12, 1), "전사", "REGISTERED")
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        statisticsExcelService.exportUnitRosterExcel(1L, response);

        // then
        byte[] content = response.getContentAsByteArray();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            XSSFSheet sheet = workbook.getSheet("사망자 명부");
            assertThat(sheet).isNotNull();
            // 주민번호 마스킹 확인 (5번째 컬럼, index 4)
            assertThat(sheet.getRow(1).getCell(4).getStringCellValue())
                    .isEqualTo("******-*******");
        }
    }

    @Test
    @DisplayName("전사망자 명부 Excel 생성 확인")
    void exportAllRosterExcel_createsValidXlsx() throws Exception {
        // given
        when(statisticsService.getRosterAll()).thenReturn(List.of(
                new DeadRosterResponse(1L, "육군", "A-11111", "김철수",
                        "******-*******", "병장", "2사단",
                        LocalDate.of(2024, 6, 15), "순직", "CONFIRMED")
        ));
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        statisticsExcelService.exportAllRosterExcel(response);

        // then
        assertThat(response.getContentType())
                .isEqualTo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        byte[] content = response.getContentAsByteArray();
        assertThat(content.length).isGreaterThan(0);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            XSSFSheet sheet = workbook.getSheet("사망자 명부");
            assertThat(sheet).isNotNull();
            // 헤더 + 데이터 1건 = 2행
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
        }
    }
}
