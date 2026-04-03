package com.navy.casualty.statistics.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * StatisticsService 단위 테스트 스켈레톤.
 * Task 2에서 프로덕션 코드 완성 후 @Disabled 제거 및 구현 전환.
 */
class StatisticsServiceTest {

    // Mock: StatisticsRepository, BranchCodeRepository, RankCodeRepository,
    //       UnitCodeRepository, DeathTypeRepository
    // InjectMocks: StatisticsService

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("STAT-01: 신분별 사망자 집계 반환")
    void getByBranch_returnsGroupedBranchStatistics() {
        // mock repository -> BranchStatResponse 2건 반환
        // service 호출 결과 2건, branchName/count 검증
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("STAT-02: 월별 사망자 집계 반환")
    void getByMonth_returnsGroupedMonthlyStatistics() {
        // mock repository -> MonthlyStatResponse mock
        // year/month/count 검증
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("STAT-03: 연도별 사망자 집계 반환")
    void getByYear_returnsGroupedYearlyStatistics() {
        // mock repository -> YearlyStatResponse mock
        // year/count 검증
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("STAT-04: 부대별 사망자 집계 반환")
    void getByUnit_returnsGroupedUnitStatistics() {
        // mock repository -> UnitStatResponse mock
        // unitName/count 검증
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("STAT-05: 부대별 사망자 명부 변환 + SSN 마스킹")
    void getRosterByUnit_filtersAndConvertsToResponse() {
        // Dead 엔티티 mock + 코드 테이블 Map mock
        // DeadRosterResponse로 변환 검증, ssnMasked 마스킹 확인
    }

    @Test
    @Disabled("RED - 프로덕션 코드 미작성")
    @DisplayName("STAT-06: 전체 사망자 명부 변환")
    void getRosterAll_convertsAllToResponse() {
        // Dead 전체 mock -> 변환 검증
    }
}
