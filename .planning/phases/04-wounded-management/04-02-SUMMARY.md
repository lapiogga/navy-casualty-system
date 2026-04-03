---
phase: 04-wounded-management
plan: 02
subsystem: ui
tags: [react, typescript, antd, react-query, wounded]

requires:
  - phase: 04-wounded-management/04-01
    provides: "상이자 백엔드 CRUD API + 보훈청 코드 API"
  - phase: 03-dead-management/03-02
    provides: "Dead 프론트엔드 패턴 (타입/훅/페이지 구조)"
provides:
  - "상이자 TypeScript 타입 (WoundedRecord, VeteransOffice, WoundType)"
  - "상이자 react-query API 훅 (CRUD + 상태변경 + 보훈청)"
  - "상이자 목록 페이지 (검색 폼 + 서버사이드 Table)"
  - "상이자 등록/수정 Modal (보훈청 Select + 병명 + 상이구분)"
  - "상이자 삭제 Modal (사유 필수)"
  - "/wounded 라우팅 연결"
affects: [04-wounded-management/04-03, 05-review-management]

tech-stack:
  added: []
  patterns: ["Dead 프론트엔드 패턴을 Wounded에 복제 + 고유 필드 추가"]

key-files:
  created:
    - frontend/src/types/wounded.ts
    - frontend/src/api/wounded.ts
    - frontend/src/pages/wounded/WoundedListPage.tsx
    - frontend/src/pages/wounded/WoundedFormModal.tsx
    - frontend/src/pages/wounded/WoundedDeleteModal.tsx
  modified:
    - frontend/src/routes/index.tsx

key-decisions:
  - "Dead 코드 훅(useRanks/useBranches/useUnits) re-export로 재사용"
  - "상이자 4단계 상태 (REGISTERED/UNDER_REVIEW/CONFIRMED/NOTIFIED)"
  - "Excel 다운로드 버튼 disabled placeholder (04-03에서 활성화)"

patterns-established:
  - "Dead 모듈 패턴 복제: 타입 -> API 훅 -> 목록 페이지 -> Form/Delete Modal -> 라우팅"

requirements-completed: [WOND-01, WOND-02, WOND-03, WOND-04, WOND-06, WOND-07]

duration: 6min
completed: 2026-04-03
---

# Phase 4 Plan 02: 상이자 프론트엔드 Summary

**상이자 CRUD 프론트엔드 완성: 보훈청 Select + 병명 + 상이구분 3-value enum + 4단계 상태 Tag 포함 목록/등록/수정/삭제 화면**

## Performance

- **Duration:** 6min
- **Started:** 2026-04-03T02:58:16Z
- **Completed:** 2026-04-03T03:04:41Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- 상이자 TypeScript 타입 7개 + react-query API 훅 7개 (CRUD 4 + 상태변경 + 보훈청 + 코드 re-export)
- 상이자 목록 페이지: 8개 검색 필드 + 접기/펼치기 + 서버사이드 페이징 Table + 4단계 상태 Tag
- 등록/수정 Modal: 보훈청 Select(showSearch) + 병명 Input + 상이구분 Select(필수) + 주민번호 체크섬 검증
- 삭제 Modal: 사유 필수 입력, 논리 삭제

## Task Commits

1. **Task 1: TypeScript 타입 + react-query API 훅** - `4df30c4` (feat)
2. **Task 2: 목록 페이지 + 등록/수정/삭제 Modal + 라우팅** - `f49182b` (feat)

## Files Created/Modified
- `frontend/src/types/wounded.ts` - 상이자 타입 정의 (WoundedRecord, VeteransOffice, WoundType 등)
- `frontend/src/api/wounded.ts` - react-query 훅 (CRUD + 상태변경 + 보훈청 코드)
- `frontend/src/pages/wounded/WoundedListPage.tsx` - 상이자 목록 + 검색 폼 화면
- `frontend/src/pages/wounded/WoundedFormModal.tsx` - 등록/수정 Modal (보훈청 Select + 병명 + 상이구분)
- `frontend/src/pages/wounded/WoundedDeleteModal.tsx` - 삭제 사유 Modal
- `frontend/src/routes/index.tsx` - /wounded 라우팅 WoundedListPage 연결

## Decisions Made
- Dead 코드 훅(useRanks/useBranches/useUnits) re-export로 재사용 (중복 코드 방지)
- 상이자 4단계 상태 (REGISTERED -> UNDER_REVIEW -> CONFIRMED -> NOTIFIED, Dead는 3단계)
- Excel 다운로드 버튼 disabled placeholder로 배치 (04-03 Plan에서 활성화 예정)

## Deviations from Plan

None - plan 그대로 실행.

## Issues Encountered
None

## User Setup Required
None - 외부 서비스 설정 불필요.

## Next Phase Readiness
- 04-03 (상이자 Excel 내보내기) 착수 가능
- 백엔드 API(04-01)와 결합하면 상이자 CRUD 전체 흐름 동작

---
*Phase: 04-wounded-management*
*Completed: 2026-04-03*
