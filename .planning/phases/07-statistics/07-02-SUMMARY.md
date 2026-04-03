---
phase: 07-statistics
plan: 02
subsystem: ui
tags: [react, ant-design-charts, react-query, typescript, excel]

requires:
  - phase: 07-statistics-01
    provides: "통계 백엔드 API 6종 (branch/monthly/yearly/unit/roster) + Excel 엔드포인트 6종"
provides:
  - "통계 현황 4개 페이지 (신분별/월별/연도별/부대별) Column 바 차트 + 합계 테이블"
  - "명부 2개 페이지 (부대별 명부 + 전사망자 명부)"
  - "API react-query 훅 12종 (조회 6 + Excel mutation 6)"
  - "사이드바 현황/명부 SubMenu 구조"
affects: [08-deployment]

tech-stack:
  added: ["@ant-design/charts"]
  patterns: ["Column 바 차트 + Table 동일 데이터 공유 패턴", "SubMenu + defaultOpenKeys 자동 open"]

key-files:
  created:
    - frontend/src/types/statistics.ts
    - frontend/src/api/statistics.ts
    - frontend/src/pages/statistics/BranchStatPage.tsx
    - frontend/src/pages/statistics/MonthlyStatPage.tsx
    - frontend/src/pages/statistics/YearlyStatPage.tsx
    - frontend/src/pages/statistics/UnitStatPage.tsx
    - frontend/src/pages/statistics/UnitRosterPage.tsx
    - frontend/src/pages/statistics/AllRosterPage.tsx
  modified:
    - frontend/src/components/layout/AppLayout.tsx
    - frontend/src/routes/index.tsx

key-decisions:
  - "@ant-design/charts Column 컴포넌트로 바 차트 구현 (recharts 대신 Ant Design 생태계 통일)"
  - "차트+테이블 동일 훅 1회 호출 데이터 공유로 중복 API 호출 방지 (STAT-07)"
  - "dead.ts useUnits 훅 재사용 (부대별 명부 부대 선택 필터)"

patterns-established:
  - "Column 차트 + Table 합계행 패턴: useXxxStat() 1회 호출 -> data를 차트 config + Table dataSource에 동일 전달"
  - "Excel Blob 다운로드 헬퍼: downloadBlob() 유틸 함수로 중복 제거"
  - "SubMenu + defaultOpenKeys: pathname 기반 자동 open"

requirements-completed: [STAT-01, STAT-02, STAT-03, STAT-04, STAT-05, STAT-06, STAT-07]

duration: 5min
completed: 2026-04-03
---

# Phase 7 Plan 2: 통계/명부 프론트엔드 Summary

**@ant-design/charts Column 바 차트 4종 + 명부 2종 + 사이드바 SubMenu + react-query 훅 12종 구현**

## Performance

- **Duration:** 5min
- **Started:** 2026-04-03T17:20:17Z
- **Completed:** 2026-04-03T17:25:00Z
- **Tasks:** 2
- **Files modified:** 10

## Accomplishments
- 4종 통계 현황 페이지: Column 바 차트 + 합계 테이블 (신분별/월별/연도별/부대별)
- 2종 명부 페이지: 부대별 명부 (부대 선택 필터) + 전사망자 명부 (필터 없음)
- react-query 조회 훅 6종 + Excel 다운로드 mutation 6종
- 사이드바 현황(4개)/명부(2개) SubMenu + pathname 기반 자동 open
- PlaceholderPage 참조 제거, 6개 라우트 추가

## Task Commits

1. **Task 1: 타입 정의 + API 훅 + @ant-design/charts 설치** - `32e72fc` (feat)
2. **Task 2: 6개 페이지 + 사이드바 메뉴 + 라우팅** - `32a35c3` (feat)

## Files Created/Modified
- `frontend/src/types/statistics.ts` - 통계 응답 타입 5종 (Branch/Monthly/Yearly/Unit/DeadRoster)
- `frontend/src/api/statistics.ts` - 조회 훅 6종 + Excel mutation 6종 + downloadBlob 헬퍼
- `frontend/src/pages/statistics/BranchStatPage.tsx` - 신분별 사망자 현황 (Column 차트 + 합계 테이블)
- `frontend/src/pages/statistics/MonthlyStatPage.tsx` - 월별 사망자 현황
- `frontend/src/pages/statistics/YearlyStatPage.tsx` - 연도별 사망자 현황
- `frontend/src/pages/statistics/UnitStatPage.tsx` - 부대별 사망자 현황
- `frontend/src/pages/statistics/UnitRosterPage.tsx` - 부대별 사망자 명부 (부대 선택 필터)
- `frontend/src/pages/statistics/AllRosterPage.tsx` - 전사망자 명부
- `frontend/src/components/layout/AppLayout.tsx` - 현황/명부 SubMenu 추가
- `frontend/src/routes/index.tsx` - 6개 라우트 추가, PlaceholderPage 제거

## Decisions Made
- @ant-design/charts Column 컴포넌트로 바 차트 구현 (Ant Design 생태계 통일)
- 차트+테이블 동일 훅 1회 호출 데이터 공유로 중복 API 호출 방지 (STAT-07)
- dead.ts useUnits 훅 재사용 (부대별 명부 부대 선택 필터)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 07 통계 기능 전체 완성 (백엔드 API + 프론트엔드 UI)
- Phase 08 배포 준비 가능

---
*Phase: 07-statistics*
*Completed: 2026-04-03*
