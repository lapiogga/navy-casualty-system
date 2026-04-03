package com.navy.casualty.statistics.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * StatisticsExcelService 단위 테스트 스켈레톤.
 * Task 2에서 프로덕션 코드 완성 후 @Disabled 제거 및 구현 전환.
 */
class StatisticsExcelServiceTest {

    // Mock: StatisticsService
    // InjectMocks: StatisticsExcelService

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("신분별 통계 Excel 생성 - Content-Type 및 바이트 검증")
    void exportBranchStatExcel_createsValidXlsx() {
        // MockHttpServletResponse에 Content-Type 확인
        // 바이트 길이 > 0
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("부대별 명부 Excel - 마스킹된 주민번호 포함 확인")
    void exportUnitRosterExcel_includesMaskedSsn() {
        // 명부 Excel에서 마스킹된 주민번호 포함 확인
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("전사망자 명부 Excel 생성 확인")
    void exportAllRosterExcel_createsValidXlsx() {
        // 전사망자 명부 Excel 정상 생성 확인
    }
}
