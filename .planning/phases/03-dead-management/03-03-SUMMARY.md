---
phase: 03-dead-management
plan: 03
subsystem: dead-excel-export
tags: [excel, poi, export, blob-download]
dependency_graph:
  requires: [03-01, 03-02]
  provides: [dead-excel-export, dead-excel-frontend]
  affects: [dead-management]
tech_stack:
  added: [apache-poi-5.3.0, SXSSFWorkbook]
  patterns: [blob-download, code-table-cache, role-based-masking]
key_files:
  created:
    - backend/src/main/java/com/navy/casualty/dead/service/DeadExcelService.java
    - backend/src/main/java/com/navy/casualty/dead/controller/DeadController.java
    - backend/src/main/java/com/navy/casualty/dead/repository/DeadRepositoryCustom.java
    - backend/src/main/java/com/navy/casualty/dead/repository/DeadRepositoryImpl.java
    - backend/src/test/java/com/navy/casualty/dead/DeadExcelServiceTest.java
    - frontend/src/api/dead.ts
    - frontend/src/pages/dead/DeadListPage.tsx
    - frontend/src/types/dead.ts
  modified:
    - backend/build.gradle.kts
decisions:
  - 코드 테이블을 Map으로 한 번에 로드하여 N+1 방지
  - SXSSFWorkbook(100) 메모리 윈도우로 대용량 안정 처리
  - 03-01/03-02 병렬 실행으로 의존 Entity/DTO/Repository stub 직접 생성
metrics:
  duration: 10min
  completed: "2026-04-03T01:41:00Z"
---

# Phase 03 Plan 03: 사망자 Excel 내보내기 Summary

Apache POI SXSSFWorkbook 기반 Excel 내보내기: GET /api/dead/excel 엔드포인트 + 역할별 주민번호 마스킹 + Blob 다운로드 프론트엔드

## What Was Done

### Task 1: Apache POI + DeadExcelService + searchAll + Controller + 테스트
- **Commit:** d481c46
- Apache POI 5.3.0 의존성 추가 (build.gradle.kts)
- DeadExcelService: SXSSFWorkbook(100) 메모리 윈도우, 15개 컬럼 헤더, 코드 테이블 Map 캐시
- RrnMaskingUtil.mask()로 역할별 주민번호 마스킹 적용
- @AuditLog(action="EXPORT", targetTable="TB_DEAD") 감사 로그
- DeadRepositoryCustom.searchAll(): 페이징 없는 전체 검색 (Excel용)
- DeadRepositoryImpl: BooleanBuilder 기반 동적 조건 8개
- DeadController GET /api/dead/excel 엔드포인트 (@PreAuthorize VIEWER 이상)
- DeadExcelServiceTest: 3개 테스트 (Content-Type, 헤더 행 15컬럼, 빈 데이터 검증)

### Task 2: 프론트엔드 Excel 다운로드 훅 + DeadListPage 버튼
- **Commit:** f83a462
- useExportDeadExcel mutation 훅: Blob responseType + 파일 다운로드 트리거
- DeadListPage: DownloadOutlined 아이콘 + loading 상태 표시
- DeadSearchParams, DeadResponse TypeScript 타입 정의
- TypeScript 컴파일 성공 확인

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 의존 Entity/DTO/Repository 직접 생성**
- **Found during:** Task 1
- **Issue:** 03-01/03-02 플랜이 병렬 실행 중이어서 Dead 엔티티, DeadSearchRequest DTO, 코드 테이블 Entity/Repository가 존재하지 않음
- **Fix:** 03-01 PLAN 사양에 맞춰 필요한 의존 파일들을 직접 생성 (Dead.java, DeadStatus.java, DeadSearchRequest.java, 코드 테이블 Entity 5개, Repository 6개)
- **Files created:** 14개 의존 파일 (merge 시 03-01과 충돌 가능, 동일 사양 기반이므로 해소 용이)
- **Commit:** d481c46

**2. [Rule 3 - Blocking] node_modules 미설치**
- **Found during:** Task 2
- **Issue:** frontend/node_modules 미존재로 tsc 검증 불가
- **Fix:** npm install 실행
- **Commit:** (검증 단계, 별도 커밋 불필요)

## Known Stubs

없음 - 모든 Excel 내보내기 기능이 완전 구현됨. DeadListPage는 최소한의 페이지 구조만 포함하나, 이는 03-02 플랜의 범위이며 Excel 다운로드 버튼은 완전 작동.

## Self-Check: PASSED
