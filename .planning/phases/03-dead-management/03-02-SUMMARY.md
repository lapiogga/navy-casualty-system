---
phase: 03-dead-management
plan: 02
subsystem: ui
tags: [react, typescript, antd, react-query, dayjs]

requires:
  - phase: 03-dead-management/01
    provides: "백엔드 사망자 CRUD API + 코드 테이블 API"
  - phase: 02-rbac
    provides: "인증/인가 인프라 (useAuth, apiClient, AuthGuard)"
provides:
  - "사망자 관리 프론트엔드 전체 (목록 + 검색 + 등록/수정/삭제 Modal)"
  - "react-query API 훅 11개 (CRUD 5 + 상태변경 1 + 코드 5)"
  - "주민번호 체크섬 검증 유틸"
  - "사망자 TypeScript 타입 정의"
affects: [03-dead-management/03, 04-wounded-management]

tech-stack:
  added: []
  patterns: ["react-query 훅 기반 API 호출 + invalidateQueries 자동 갱신", "검색 폼 Card 접기/펼치기 + 서버사이드 Table 패턴"]

key-files:
  created:
    - frontend/src/types/dead.ts
    - frontend/src/utils/rrnValidation.ts
    - frontend/src/api/dead.ts
    - frontend/src/pages/dead/DeadListPage.tsx
    - frontend/src/pages/dead/DeadFormModal.tsx
    - frontend/src/pages/dead/DeadDeleteModal.tsx
    - frontend/src/__tests__/rrnValidation.test.ts
  modified:
    - frontend/src/routes/index.tsx

key-decisions:
  - "axios.isAxiosError 타입 가드로 onError에서 any 타입 완전 제거"
  - "주민번호 입력 시 생년월일 자동 추출 (7번째 자리로 세기 판별)"

patterns-established:
  - "react-query deadKeys 패턴: all > lists > list(params) 계층 구조"
  - "검색 폼 접기/펼치기 Card + 서버사이드 페이징 Table 조합"
  - "Modal에서 mutation onSuccess 콜백으로 폼 리셋 + 닫기"

requirements-completed: [DEAD-01, DEAD-02, DEAD-03, DEAD-04, DEAD-06, DEAD-07]

duration: 8min
completed: 2026-04-03
---

# Phase 3 Plan 2: 사망자 프론트엔드 Summary

**react-query 기반 사망자 CRUD 프론트엔드 + 주민번호 체크섬 검증 + 서버사이드 페이징 검색 화면**

## Performance

- **Duration:** 8min
- **Started:** 2026-04-03T01:08:25Z
- **Completed:** 2026-04-03T01:16:38Z
- **Tasks:** 2
- **Files modified:** 8

## Accomplishments
- 사망자 목록 페이지 (검색 폼 8필드 접기/펼치기 + 서버사이드 Table + 상태 Tag)
- 등록/수정 Modal (주민번호 체크섬 검증 + 생년월일 자동 추출)
- 삭제 Modal (사유 필수 입력)
- react-query API 훅 11개 (CRUD 5 + 상태변경 1 + 코드테이블 5)
- 역할별 버튼 표시 (OPERATOR: 수정, MANAGER: 삭제/상태변경)

## Task Commits

Each task was committed atomically:

1. **Task 1: TypeScript 타입 + 주민번호 검증 유틸 + react-query API 훅** - `2e5b69b` (feat)
2. **Task 2: 사망자 목록 페이지 + Modal + 라우팅 연결** - `077dfeb` (feat)

## Files Created/Modified
- `frontend/src/types/dead.ts` - DeadRecord, DeadSearchParams 등 타입 정의
- `frontend/src/utils/rrnValidation.ts` - 주민번호 체크섬 검증 (가중치 mod 11)
- `frontend/src/api/dead.ts` - react-query 훅 11개 (CRUD + 코드테이블)
- `frontend/src/__tests__/rrnValidation.test.ts` - 주민번호 검증 테스트 10건
- `frontend/src/pages/dead/DeadListPage.tsx` - 사망자 목록 + 검색 폼 화면
- `frontend/src/pages/dead/DeadFormModal.tsx` - 등록/수정 Modal
- `frontend/src/pages/dead/DeadDeleteModal.tsx` - 삭제 사유 Modal
- `frontend/src/routes/index.tsx` - /dead 라우트를 DeadListPage로 교체

## Decisions Made
- axios.isAxiosError 타입 가드로 onError에서 any 타입 완전 제거
- 주민번호 입력 시 7번째 자리(성별코드)로 세기 판별하여 생년월일 자동 추출

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- npm install 필요 (worktree에서 node_modules 미설치) - npm install로 해결

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- 사망자 프론트엔드 완성, 백엔드 API(03-01)와 결합 가능
- Excel 다운로드 버튼은 disabled placeholder (03-03에서 구현)
- 상이자 관리(Phase 4)에서 동일 패턴 재사용 가능

---
*Phase: 03-dead-management*
*Completed: 2026-04-03*
