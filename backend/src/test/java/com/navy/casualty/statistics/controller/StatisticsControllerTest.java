package com.navy.casualty.statistics.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.DeadRosterResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;
import com.navy.casualty.statistics.service.StatisticsExcelService;
import com.navy.casualty.statistics.service.StatisticsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * StatisticsController 슬라이스 테스트.
 * @WebMvcTest로 웹 계층만 로드하여 엔드포인트 200 응답 및 JSON 구조를 검증한다.
 */
@WebMvcTest(StatisticsController.class)
class StatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatisticsService statisticsService;

    @MockitoBean
    private StatisticsExcelService statisticsExcelService;

    @Test
    @DisplayName("GET /api/statistics/branch -> 200 + JSON 구조")
    @WithMockUser(roles = "VIEWER")
    void getBranch_returns200WithStatData() throws Exception {
        when(statisticsService.getByBranch()).thenReturn(List.of(
                new BranchStatResponse("육군", 10L)
        ));

        mockMvc.perform(get("/api/statistics/branch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].branchName").value("육군"))
                .andExpect(jsonPath("$.data[0].count").value(10));
    }

    @Test
    @DisplayName("GET /api/statistics/monthly -> 200")
    @WithMockUser(roles = "VIEWER")
    void getMonthly_returns200() throws Exception {
        when(statisticsService.getByMonth()).thenReturn(List.of(
                new MonthlyStatResponse(2025, 12, 3L)
        ));

        mockMvc.perform(get("/api/statistics/monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].year").value(2025))
                .andExpect(jsonPath("$.data[0].month").value(12));
    }

    @Test
    @DisplayName("GET /api/statistics/yearly -> 200")
    @WithMockUser(roles = "VIEWER")
    void getYearly_returns200() throws Exception {
        when(statisticsService.getByYear()).thenReturn(List.of(
                new YearlyStatResponse(2025, 15L)
        ));

        mockMvc.perform(get("/api/statistics/yearly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].year").value(2025));
    }

    @Test
    @DisplayName("GET /api/statistics/unit -> 200")
    @WithMockUser(roles = "VIEWER")
    void getUnit_returns200() throws Exception {
        when(statisticsService.getByUnit()).thenReturn(List.of(
                new UnitStatResponse("1함대", 8L)
        ));

        mockMvc.perform(get("/api/statistics/unit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].unitName").value("1함대"));
    }

    @Test
    @DisplayName("GET /api/statistics/roster/unit?unitId=1 -> 200")
    @WithMockUser(roles = "VIEWER")
    void getRosterByUnit_returns200() throws Exception {
        when(statisticsService.getRosterByUnit(1L)).thenReturn(List.of(
                new DeadRosterResponse(1L, "해군", "N-12345", "홍길동",
                        "******-*******", "상병", "1함대",
                        LocalDate.of(2025, 12, 1), "전사", "REGISTERED")
        ));

        mockMvc.perform(get("/api/statistics/roster/unit")
                        .param("unitId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].serviceNumber").value("N-12345"));
    }

    @Test
    @DisplayName("GET /api/statistics/roster/all -> 200")
    @WithMockUser(roles = "VIEWER")
    void getRosterAll_returns200() throws Exception {
        when(statisticsService.getRosterAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/statistics/roster/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("GET /api/statistics/branch/excel -> 200 + content-type")
    @WithMockUser(roles = "VIEWER")
    void exportBranchExcel_returns200() throws Exception {
        mockMvc.perform(get("/api/statistics/branch/excel"))
                .andExpect(status().isOk());
    }
}
