package com.navy.casualty.statistics.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.navy.casualty.code.entity.BranchCode;
import com.navy.casualty.code.entity.DeathType;
import com.navy.casualty.code.entity.RankCode;
import com.navy.casualty.code.entity.UnitCode;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.DeadRosterResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;
import com.navy.casualty.statistics.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 통계 서비스.
 * 4종 집계(신분별/월별/연도별/부대별) + 2종 명부(부대별/전체) 조회를 제공한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final StatisticsRepository statisticsRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final DeathTypeRepository deathTypeRepository;

    public List<BranchStatResponse> getByBranch() {
        return statisticsRepository.getByBranch();
    }

    public List<MonthlyStatResponse> getByMonth() {
        return statisticsRepository.getByMonth();
    }

    public List<YearlyStatResponse> getByYear() {
        return statisticsRepository.getByYear();
    }

    public List<UnitStatResponse> getByUnit() {
        return statisticsRepository.getByUnit();
    }

    /**
     * 부대별 사망자 명부를 조회한다.
     * 코드 테이블 Map 캐시로 N+1 방지, 주민번호 역할별 마스킹 적용.
     */
    public List<DeadRosterResponse> getRosterByUnit(Long unitId) {
        Map<Long, String> rankMap = buildRankMap();
        Map<Long, String> branchMap = buildBranchMap();
        Map<Long, String> unitMap = buildUnitMap();
        Map<Long, String> deathTypeMap = buildDeathTypeMap();
        String userRole = getCurrentUserRole();

        return statisticsRepository.getRosterByUnit(unitId).stream()
                .map(dead -> toRosterResponse(dead, rankMap, branchMap, unitMap, deathTypeMap, userRole))
                .toList();
    }

    /**
     * 전체 사망자 명부를 조회한다.
     */
    public List<DeadRosterResponse> getRosterAll() {
        Map<Long, String> rankMap = buildRankMap();
        Map<Long, String> branchMap = buildBranchMap();
        Map<Long, String> unitMap = buildUnitMap();
        Map<Long, String> deathTypeMap = buildDeathTypeMap();
        String userRole = getCurrentUserRole();

        return statisticsRepository.getRosterAll().stream()
                .map(dead -> toRosterResponse(dead, rankMap, branchMap, unitMap, deathTypeMap, userRole))
                .toList();
    }

    /**
     * Dead 엔티티를 DeadRosterResponse로 변환한다.
     */
    private DeadRosterResponse toRosterResponse(Dead dead,
            Map<Long, String> rankMap, Map<Long, String> branchMap,
            Map<Long, String> unitMap, Map<Long, String> deathTypeMap,
            String userRole) {
        return new DeadRosterResponse(
                dead.getId(),
                dead.getBranchId() != null ? branchMap.getOrDefault(dead.getBranchId(), "미분류") : "미분류",
                dead.getServiceNumber(),
                dead.getName(),
                RrnMaskingUtil.mask(dead.getSsnEncrypted(), userRole),
                dead.getRankId() != null ? rankMap.getOrDefault(dead.getRankId(), "") : "",
                dead.getUnitId() != null ? unitMap.getOrDefault(dead.getUnitId(), "미분류") : "미분류",
                dead.getDeathDate(),
                dead.getDeathTypeId() != null ? deathTypeMap.getOrDefault(dead.getDeathTypeId(), "") : "",
                dead.getStatus().name()
        );
    }

    private Map<Long, String> buildRankMap() {
        return rankCodeRepository.findAll().stream()
                .collect(Collectors.toMap(RankCode::getId, RankCode::getRankName));
    }

    private Map<Long, String> buildBranchMap() {
        return branchCodeRepository.findAll().stream()
                .collect(Collectors.toMap(BranchCode::getId, BranchCode::getBranchName));
    }

    private Map<Long, String> buildUnitMap() {
        return unitCodeRepository.findAll().stream()
                .collect(Collectors.toMap(UnitCode::getId, UnitCode::getUnitName));
    }

    private Map<Long, String> buildDeathTypeMap() {
        return deathTypeRepository.findAll().stream()
                .collect(Collectors.toMap(DeathType::getId, DeathType::getTypeName));
    }

    /**
     * 현재 인증된 사용자의 역할을 추출한다.
     */
    private String getCurrentUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream().findFirst()
                .map(GrantedAuthority::getAuthority).orElse("ROLE_VIEWER");
    }
}
