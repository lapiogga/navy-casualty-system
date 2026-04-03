package com.navy.casualty.statistics.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * StatisticsController 테스트 스켈레톤.
 * Task 2에서 프로덕션 코드 완성 후 @Disabled 제거 및 구현 전환.
 */
class StatisticsControllerTest {

    // MockBean: StatisticsService, StatisticsExcelService
    // AutoConfigureMockMvc + SpringBootTest

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/branch -> 200 + JSON 구조")
    void getBranch_returns200WithStatData() {
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/monthly -> 200")
    void getMonthly_returns200() {
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/yearly -> 200")
    void getYearly_returns200() {
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/unit -> 200")
    void getUnit_returns200() {
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/roster/unit?unitId=1 -> 200")
    void getRosterByUnit_returns200() {
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/roster/all -> 200")
    void getRosterAll_returns200() {
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("GET /api/statistics/branch/excel -> 200 + content-type")
    void exportBranchExcel_returns200() {
    }
}
