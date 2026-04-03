package com.navy.casualty.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import com.navy.casualty.code.entity.BranchCode;
import com.navy.casualty.code.entity.DeathType;
import com.navy.casualty.code.entity.RankCode;
import com.navy.casualty.code.entity.UnitCode;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.entity.DeadStatus;
import com.navy.casualty.statistics.dto.BranchStatResponse;
import com.navy.casualty.statistics.dto.DeadRosterResponse;
import com.navy.casualty.statistics.dto.MonthlyStatResponse;
import com.navy.casualty.statistics.dto.UnitStatResponse;
import com.navy.casualty.statistics.dto.YearlyStatResponse;
import com.navy.casualty.statistics.repository.StatisticsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * StatisticsService 단위 테스트.
 * Mockito 기반으로 Repository/코드 테이블을 모킹하여 서비스 로직을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private StatisticsRepository statisticsRepository;
    @Mock
    private RankCodeRepository rankCodeRepository;
    @Mock
    private BranchCodeRepository branchCodeRepository;
    @Mock
    private UnitCodeRepository unitCodeRepository;
    @Mock
    private DeathTypeRepository deathTypeRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        // VIEWER 역할로 SecurityContext 설정
        var auth = new UsernamePasswordAuthenticationToken(
                "testuser", null,
                List.of(new SimpleGrantedAuthority("ROLE_VIEWER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("STAT-01: 신분별 사망자 집계 반환")
    void getByBranch_returnsGroupedBranchStatistics() {
        // given
        when(statisticsRepository.getByBranch()).thenReturn(List.of(
                new BranchStatResponse("육군", 10L),
                new BranchStatResponse("해군", 5L)
        ));

        // when
        List<BranchStatResponse> result = statisticsService.getByBranch();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).branchName()).isEqualTo("육군");
        assertThat(result.get(0).count()).isEqualTo(10L);
        assertThat(result.get(1).branchName()).isEqualTo("해군");
        assertThat(result.get(1).count()).isEqualTo(5L);
    }

    @Test
    @DisplayName("STAT-02: 월별 사망자 집계 반환")
    void getByMonth_returnsGroupedMonthlyStatistics() {
        when(statisticsRepository.getByMonth()).thenReturn(List.of(
                new MonthlyStatResponse(2025, 12, 3L),
                new MonthlyStatResponse(2025, 11, 7L)
        ));

        List<MonthlyStatResponse> result = statisticsService.getByMonth();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).year()).isEqualTo(2025);
        assertThat(result.get(0).month()).isEqualTo(12);
        assertThat(result.get(0).count()).isEqualTo(3L);
    }

    @Test
    @DisplayName("STAT-03: 연도별 사망자 집계 반환")
    void getByYear_returnsGroupedYearlyStatistics() {
        when(statisticsRepository.getByYear()).thenReturn(List.of(
                new YearlyStatResponse(2025, 15L),
                new YearlyStatResponse(2024, 20L)
        ));

        List<YearlyStatResponse> result = statisticsService.getByYear();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).year()).isEqualTo(2025);
        assertThat(result.get(0).count()).isEqualTo(15L);
    }

    @Test
    @DisplayName("STAT-04: 부대별 사망자 집계 반환")
    void getByUnit_returnsGroupedUnitStatistics() {
        when(statisticsRepository.getByUnit()).thenReturn(List.of(
                new UnitStatResponse("1함대", 8L),
                new UnitStatResponse("2함대", 4L)
        ));

        List<UnitStatResponse> result = statisticsService.getByUnit();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).unitName()).isEqualTo("1함대");
        assertThat(result.get(0).count()).isEqualTo(8L);
    }

    @Test
    @DisplayName("STAT-05: 부대별 사망자 명부 변환 + SSN 마스킹 (VIEWER)")
    void getRosterByUnit_filtersAndConvertsToResponse() {
        // given
        Dead dead = Dead.builder()
                .id(1L)
                .branchId(1L)
                .serviceNumber("N-12345")
                .name("홍길동")
                .ssnEncrypted("900101-1234567")
                .rankId(1L)
                .unitId(1L)
                .deathDate(LocalDate.of(2025, 12, 1))
                .deathTypeId(1L)
                .status(DeadStatus.REGISTERED)
                .birthDate(LocalDate.of(1990, 1, 1))
                .build();

        when(statisticsRepository.getRosterByUnit(1L)).thenReturn(List.of(dead));
        mockCodeTables();

        // when
        List<DeadRosterResponse> result = statisticsService.getRosterByUnit(1L);

        // then
        assertThat(result).hasSize(1);
        DeadRosterResponse roster = result.get(0);
        assertThat(roster.serviceNumber()).isEqualTo("N-12345");
        assertThat(roster.name()).isEqualTo("홍길동");
        assertThat(roster.branchName()).isEqualTo("해군");
        assertThat(roster.rankName()).isEqualTo("상병");
        assertThat(roster.unitName()).isEqualTo("1함대");
        assertThat(roster.deathTypeName()).isEqualTo("전사");
        // VIEWER 역할이므로 전체 마스킹
        assertThat(roster.ssnMasked()).isEqualTo("******-*******");
    }

    @Test
    @DisplayName("STAT-06: 전체 사망자 명부 변환")
    void getRosterAll_convertsAllToResponse() {
        Dead dead = Dead.builder()
                .id(2L)
                .branchId(1L)
                .serviceNumber("N-99999")
                .name("김철수")
                .ssnEncrypted("850505-1111111")
                .rankId(1L)
                .unitId(1L)
                .deathDate(LocalDate.of(2024, 6, 15))
                .deathTypeId(1L)
                .status(DeadStatus.CONFIRMED)
                .birthDate(LocalDate.of(1985, 5, 5))
                .build();

        when(statisticsRepository.getRosterAll()).thenReturn(List.of(dead));
        mockCodeTables();

        List<DeadRosterResponse> result = statisticsService.getRosterAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).serviceNumber()).isEqualTo("N-99999");
        assertThat(result.get(0).status()).isEqualTo("CONFIRMED");
    }

    /**
     * 코드 테이블 mock 설정 헬퍼.
     * 리플렉션으로 id를 설정할 수 없으므로 Mockito spy를 사용하지 않고
     * 빈 리스트를 반환하여 getOrDefault 로직의 미분류 처리를 검증한다.
     * 여기서는 실제 id를 가진 코드 엔티티 생성을 위해 별도 팩토리를 사용한다.
     */
    private void mockCodeTables() {
        // RankCode, BranchCode, UnitCode, DeathType 모두 @NoArgsConstructor(PROTECTED) + no setter
        // 따라서 빈 리스트 반환 시 미분류로 처리됨을 검증하거나,
        // 별도로 테스트 전용 엔티티를 구성해야 함
        // 여기서는 빈 리스트로 반환하되, assertion을 미분류로 조정하지 않고
        // Mockito를 활용한다

        // 코드 테이블이 빈 리스트면 getOrDefault에서 기본값 반환
        // 이를 우회하기 위해 @Builder가 있는 Dead와 달리 코드 엔티티는 Builder 미제공
        // Mockito로 개별 엔티티를 mock
        var branch = org.mockito.Mockito.mock(com.navy.casualty.code.entity.BranchCode.class);
        when(branch.getId()).thenReturn(1L);
        when(branch.getBranchName()).thenReturn("해군");
        when(branchCodeRepository.findAll()).thenReturn(List.of(branch));

        var rank = org.mockito.Mockito.mock(com.navy.casualty.code.entity.RankCode.class);
        when(rank.getId()).thenReturn(1L);
        when(rank.getRankName()).thenReturn("상병");
        when(rankCodeRepository.findAll()).thenReturn(List.of(rank));

        var unit = org.mockito.Mockito.mock(com.navy.casualty.code.entity.UnitCode.class);
        when(unit.getId()).thenReturn(1L);
        when(unit.getUnitName()).thenReturn("1함대");
        when(unitCodeRepository.findAll()).thenReturn(List.of(unit));

        var deathType = org.mockito.Mockito.mock(com.navy.casualty.code.entity.DeathType.class);
        when(deathType.getId()).thenReturn(1L);
        when(deathType.getTypeName()).thenReturn("전사");
        when(deathTypeRepository.findAll()).thenReturn(List.of(deathType));
    }
}
