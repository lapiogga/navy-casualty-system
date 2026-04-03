package com.navy.casualty.statistics.repository;

import java.util.List;

import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;

/**
 * 통계 집계 쿼리 리포지토리 인터페이스.
 */
public interface StatisticsRepository {

    List<BranchStatResponse> getByBranch();

    List<MonthlyStatResponse> getByMonth();

    List<YearlyStatResponse> getByYear();

    List<UnitStatResponse> getByUnit();

    List<Dead> getRosterByUnit(Long unitId);

    List<Dead> getRosterAll();
}
