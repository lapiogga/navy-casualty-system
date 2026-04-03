---
phase: 07-statistics
plan: 01
subsystem: api
tags: [querydsl, statistics, excel, poi, spring-boot]

requires:
  - phase: 01-foundation
    provides: Dead 엔티티, 코드 테이블, QueryDSL 설정
  - phase: 03-dead-crud
    provides: DeadExcelService 패턴, RrnMaskingUtil
provides:
  - 4종 통계 집계 API (신분별/월별/연도별/부대별)
  - 2종 명부 조회 API (부대별/전체)
  - 6종 Excel 다운로드 API
  - PostgreSQL partial index 3종
affects: [07-02-statistics-frontend]

tech-stack:
  added: []
  patterns: [QueryDSL Projections.constructor GROUP BY 집계, CaseBuilder COALESCE 미분류 처리, EXTRACT 날짜 함수]

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/statistics/controller/StatisticsController.java
    - backend/src/main/java/com/navy/casualty/statistics/service/StatisticsService.java
    - backend/src/main/java/com/navy/casualty/statistics/service/StatisticsExcelService.java
    - backend/src/main/java/com/navy/casualty/statistics/repository/StatisticsRepositoryImpl.java
    - backend/src/main/java/com/navy/casualty/statistics/dto/BranchStatResponse.java
    - backend/src/main/java/com/navy/casualty/statistics/dto/MonthlyStatResponse.java
    - backend/src/main/java/com/navy/casualty/statistics/dto/YearlyStatResponse.java
    - backend/src/main/java/com/navy/casualty/statistics/dto/UnitStatResponse.java
    - backend/src/main/java/com/navy/casualty/statistics/dto/DeadRosterResponse.java
    - backend/src/main/resources/db/migration/V17__add_statistics_indexes.sql
  modified: []

key-decisions:
  - "QueryDSL Projections.constructor로 집계 결과를 DTO에 직접 매핑 (N+1 방지)"
  - "CaseBuilder로 NULL branch/unit을 미분류로 처리 (COALESCE 대체)"
  - "@WebMvcTest 슬라이스 테스트로 컨트롤러 검증 (PiiEncryptionConverter 환경변수 의존 회피)"
  - "코드 테이블 Map 캐시 패턴으로 명부 변환 시 N+1 방지"

patterns-established:
  - "Statistics 패키지 구조: dto/repository/service/controller 4계층"
  - "Excel 내보내기 합계행 패턴: 데이터 행 후 합계 행 추가"
  - "명부 Excel 공통 메서드 writeRosterExcel() 재사용 패턴"

requirements-completed: [STAT-01, STAT-02, STAT-03, STAT-04, STAT-05, STAT-06, STAT-07]

duration: 13min
completed: 2026-04-03
---

# Phase 7 Plan 1: Statistics Backend API Summary

**QueryDSL GROUP BY 집계 4종 + 명부 2종 + Excel 6종 통계 백엔드 API 완성 (12개 엔드포인트)**

## Performance

- **Duration:** 13 min
- **Started:** 2026-04-03T17:03:43Z
- **Completed:** 2026-04-03T17:17:13Z
- **Tasks:** 3
- **Files modified:** 14

## Accomplishments
- 4종 통계 집계 API (신분별/월별/연도별/부대별) QueryDSL GROUP BY + Projections.constructor로 구현
- 2종 명부 API (부대별/전체) Dead 엔티티 -> DeadRosterResponse 변환, SSN 역할별 마스킹 적용
- 6종 Excel 다운로드 API (SXSSFWorkbook 100행 윈도우, 합계행 포함)
- PostgreSQL partial index 3종 (branch_id, unit_id, death_date WHERE deleted_at IS NULL)
- Mockito 단위 테스트 6개 + Excel 검증 3개 + WebMvcTest 슬라이스 7개 = 16개 테스트 GREEN

## Task Commits

1. **Task 0: Wave 0 테스트 스켈레톤** - `f85c791` (test)
2. **Task 1: DTO 5종 + Repository + V17 인덱스** - `3e86d16` (feat)
3. **Task 2: Service + ExcelService + Controller + 테스트 GREEN** - `0087b17` (feat)

## Files Created/Modified
- `backend/src/main/java/com/navy/casualty/statistics/controller/StatisticsController.java` - 12개 엔드포인트 (조회6 + Excel6)
- `backend/src/main/java/com/navy/casualty/statistics/service/StatisticsService.java` - 4종 집계 + 2종 명부 조회
- `backend/src/main/java/com/navy/casualty/statistics/service/StatisticsExcelService.java` - 6종 Excel 내보내기
- `backend/src/main/java/com/navy/casualty/statistics/repository/StatisticsRepositoryImpl.java` - QueryDSL 6종 집계 쿼리
- `backend/src/main/java/com/navy/casualty/statistics/repository/StatisticsRepository.java` - 리포지토리 인터페이스
- `backend/src/main/java/com/navy/casualty/statistics/dto/*.java` - DTO 5종 (record)
- `backend/src/main/resources/db/migration/V17__add_statistics_indexes.sql` - partial index 3종
- `backend/src/test/java/com/navy/casualty/statistics/service/StatisticsServiceTest.java` - 6개 Mockito 테스트
- `backend/src/test/java/com/navy/casualty/statistics/service/StatisticsExcelServiceTest.java` - 3개 Excel 검증
- `backend/src/test/java/com/navy/casualty/statistics/controller/StatisticsControllerTest.java` - 7개 WebMvcTest

## Decisions Made
- QueryDSL Projections.constructor로 집계 결과를 DTO에 직접 매핑 (N+1 방지)
- CaseBuilder로 NULL branch/unit을 "미분류"로 처리 (leftJoin + COALESCE 대체)
- @WebMvcTest 슬라이스 테스트로 컨트롤러 검증 (PiiEncryptionConverter 환경변수 의존 회피)
- 코드 테이블 Map 캐시 패턴으로 명부 변환 시 N+1 방지 (DeadExcelService 패턴 복제)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] @WebMvcTest 슬라이스 테스트로 전환**
- **Found during:** Task 2 (StatisticsControllerTest)
- **Issue:** @SpringBootTest + @MockitoBean 조합이 PiiEncryptionConverter의 PII_ENCRYPTION_KEY 환경변수 미설정으로 컨텍스트 로딩 실패
- **Fix:** @WebMvcTest(StatisticsController.class) 슬라이스 테스트로 전환, JPA 계층 미로드
- **Files modified:** StatisticsControllerTest.java
- **Verification:** 7개 테스트 모두 GREEN
- **Committed in:** 0087b17

---

**Total deviations:** 1 auto-fixed (1 blocking)
**Impact on plan:** 테스트 방식만 변경, 기능적 영향 없음.

## Issues Encountered
None

## Known Stubs
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- 12개 통계 REST API 준비 완료, 프론트엔드(07-02)에서 호출 가능
- V17 인덱스 마이그레이션 준비 (PostgreSQL 환경에서 자동 적용)

---
*Phase: 07-statistics*
*Completed: 2026-04-03*
