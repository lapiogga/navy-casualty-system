---
phase: 06-document-output
plan: 02
subsystem: document
tags: [react, antd, iframe-pdf, document-print, modal]

requires:
  - phase: 06-document-output
    provides: DocumentController REST API (PDF 생성 + 발급 이력 조회)
provides:
  - DocumentType enum + 한글 라벨 매핑 (프론트엔드)
  - useGenerateDocument / useDocumentIssues API 훅
  - DocumentIssuePurposeModal (발급 목적 입력)
  - DocumentPreviewModal (iframe PDF 뷰어 + 인쇄/다운로드)
  - 사망자/상이자/심사 목록 페이지 문서 출력 버튼
  - DocumentIssueHistoryPage (발급 이력 조회 + 검색 + 페이징)
  - /document/history 라우트 + 사이드 메뉴
affects: [06-03, frontend-integration]

tech-stack:
  added: []
  patterns: [iframe PDF 미리보기, URL.createObjectURL/revokeObjectURL Blob 관리, Dropdown 메뉴 문서 선택]

key-files:
  created:
    - frontend/src/types/document.ts
    - frontend/src/api/document.ts
    - frontend/src/pages/document/DocumentIssuePurposeModal.tsx
    - frontend/src/pages/document/DocumentPreviewModal.tsx
    - frontend/src/pages/document/PrintStyles.css
    - frontend/src/pages/document/DocumentIssueHistoryPage.tsx
  modified:
    - frontend/src/pages/dead/DeadListPage.tsx
    - frontend/src/pages/wounded/WoundedListPage.tsx
    - frontend/src/pages/review/ReviewListPage.tsx
    - frontend/src/routes/index.tsx
    - frontend/src/components/layout/AppLayout.tsx

key-decisions:
  - "iframe + contentWindow.print()로 PDF 인쇄 (react-to-print 미사용, 브라우저 네이티브 PDF 뷰어 활용)"
  - "URL.createObjectURL + useEffect cleanup으로 Blob URL 메모리 누수 방지"
  - "사망자 목록 Dropdown으로 2종 문서 선택, 상이자/심사는 단일 버튼"

patterns-established:
  - "문서 출력 흐름: 출력 버튼 -> DocumentIssuePurposeModal -> useGenerateDocument -> DocumentPreviewModal"
  - "리스트형 보고서 (targetId 불필요): targetId=0 전달"

requirements-completed: [DOCU-01, DOCU-02, DOCU-03, DOCU-04, DOCU-05, DOCU-06, DOCU-07, DOCU-08, DOCU-09]

duration: 6min
completed: 2026-04-04
---

# Phase 6 Plan 2: 문서 출력 프론트엔드 UI Summary

**사망자/상이자/심사 목록에 문서 출력 Dropdown + 발급 목적 Modal + iframe PDF 미리보기/인쇄/다운로드 + 발급 이력 조회 페이지**

## Performance

- **Duration:** 6min
- **Started:** 2026-04-03T15:10:47Z
- **Completed:** 2026-04-03T15:16:38Z
- **Tasks:** 2
- **Files modified:** 11

## Accomplishments
- DocumentType enum 7종 + API 훅 (useGenerateDocument, useDocumentIssues) + 발급 목적/PDF 미리보기 Modal 컴포넌트
- 사망자(2종 Dropdown), 상이자(1종), 심사(1종) 목록에 문서 출력 버튼 + 현황 보고서 버튼 추가
- 발급 이력 조회 페이지 (문서유형/발급자/기간 검색 + 페이징 테이블 + 발급대장 출력)
- /document/history 라우트 + 사이드 메뉴 "발급 이력" 항목

## Task Commits

Each task was committed atomically:

1. **Task 1: 문서 타입/API 훅 + 발급 목적 Modal + PDF 미리보기 Modal** - `f0f7d28` (feat)
2. **Task 2: 목록 페이지 문서 출력 버튼 + 발급 이력 조회 페이지 + 라우팅** - `8744c91` (feat)

## Files Created/Modified
- `frontend/src/types/document.ts` - DocumentType enum 7종 + 한글 라벨 + 인터페이스
- `frontend/src/api/document.ts` - useGenerateDocument (PDF Blob) + useDocumentIssues 훅
- `frontend/src/pages/document/DocumentIssuePurposeModal.tsx` - 발급 목적 TextArea Modal
- `frontend/src/pages/document/DocumentPreviewModal.tsx` - iframe PDF 뷰어 + 인쇄/다운로드
- `frontend/src/pages/document/PrintStyles.css` - @page A4 portrait 인쇄 스타일
- `frontend/src/pages/document/DocumentIssueHistoryPage.tsx` - 발급 이력 검색/조회 페이지
- `frontend/src/pages/dead/DeadListPage.tsx` - 문서출력 Dropdown 컬럼 + 현황 보고서 버튼
- `frontend/src/pages/wounded/WoundedListPage.tsx` - 확인서 출력 버튼 + 현황 보고서 버튼
- `frontend/src/pages/review/ReviewListPage.tsx` - 심사결과서 출력 버튼
- `frontend/src/routes/index.tsx` - /document/history 라우트
- `frontend/src/components/layout/AppLayout.tsx` - 사이드 메뉴 '발급 이력' 항목

## Decisions Made
- iframe + contentWindow.print()로 PDF 인쇄 구현 (react-to-print 의존성 추가 불필요)
- URL.createObjectURL로 Blob URL 생성, useEffect cleanup에서 revokeObjectURL 호출
- 사망자 목록은 Dropdown으로 2종 문서 선택 (확인서/순직확인서), 상이자/심사는 단일 버튼

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] react-to-print 설치 생략**
- **Found during:** Task 1
- **Issue:** Plan에서 react-to-print 설치를 지시했으나 실제 구현은 iframe.contentWindow.print() 사용
- **Fix:** react-to-print 의존성 추가하지 않음 (불필요한 의존성)
- **Files modified:** 없음 (package.json 변경 불필요)

---

**Total deviations:** 1 auto-fixed (1 blocking - 불필요 의존성 생략)
**Impact on plan:** 의도된 동작과 동일. 오히려 의존성 최소화.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Known Stubs
None - 모든 문서 출력 버튼이 실제 백엔드 API와 연결되며, 발급 이력 페이지도 실데이터를 조회한다.

## Next Phase Readiness
- 문서 출력 프론트엔드 UI 완성, 백엔드 API와 완전 연동 준비
- 06-03 (검증/마무리) 진행 가능

## Self-Check: PASSED
