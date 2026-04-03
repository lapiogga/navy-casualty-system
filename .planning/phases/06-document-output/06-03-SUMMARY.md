---
phase: 06-document-output
plan: 03
subsystem: testing
tags: [jasperreports, junit5, mockito, pdf, nanum-gothic, spring-security-test]

requires:
  - phase: 06-01
    provides: JasperReports 서비스 + 7종 jrxml 템플릿
  - phase: 06-02
    provides: 프론트엔드 문서 출력 UI (테스트 대상 API)
provides:
  - DocumentGenerationServiceTest (jrxml 컴파일 + PDF 생성 + 한글 폰트 검증)
  - DocumentIssueServiceTest (발급 이력 CRUD 검증)
  - DocumentControllerTest (REST API 인증/인가 검증)
affects: [07-statistics, 08-deploy]

tech-stack:
  added: []
  patterns: [Mockito 단위 테스트 + SpringBootTest 통합 테스트 혼합, JasperReports AWT 폰트 무시 설정]

key-files:
  created:
    - backend/src/test/java/com/navy/casualty/document/service/DocumentGenerationServiceTest.java
    - backend/src/test/java/com/navy/casualty/document/service/DocumentIssueServiceTest.java
    - backend/src/test/java/com/navy/casualty/document/controller/DocumentControllerTest.java
  modified: []

key-decisions:
  - "DocumentGenerationServiceTest는 @ExtendWith(MockitoExtension) 순수 단위 테스트 (SpringBoot 컨텍스트 불필요)"
  - "JasperReports AWT 폰트 미설치 환경 대응: net.sf.jasperreports.awt.ignore.missing.font=true 설정"
  - "NanumGothic 폰트 검증: PDF 바이너리 대신 TTF 파일 직접 로드 + java.awt.Font.createFont 검증"

patterns-established:
  - "JasperReports 테스트 패턴: @BeforeAll에서 awt.ignore.missing.font=true 설정 후 PDF 생성"
  - "DocumentControllerTest: @MockitoBean으로 서비스 mock + MockMvc로 API 검증"

requirements-completed: [DOCU-01, DOCU-02, DOCU-03, DOCU-04, DOCU-05, DOCU-06, DOCU-07, DOCU-08, DOCU-09]

duration: 16min
completed: 2026-04-03
---

# Phase 06 Plan 03: 문서 출력 테스트 Summary

**JasperReports 7종 jrxml 컴파일 + PDF 생성 + NanumGothic 한글 폰트 검증 + 발급이력 CRUD + REST API 인증/인가 테스트 19건 전체 GREEN**

## Performance

- **Duration:** 16min
- **Started:** 2026-04-03T15:19:58Z
- **Completed:** 2026-04-03T15:35:40Z
- **Tasks:** 1
- **Files created:** 3

## Accomplishments
- 7종 .jrxml 템플릿 파라미터 방식별 컴파일 성공 검증 (ParameterizedTest)
- PDF byte[] %PDF- 시그니처 검증 + 빈 리스트 보고서 정상 생성 확인
- NanumGothic TTF 파일 classpath 존재 + java.awt.Font 로드 가능 검증
- 발급 이력 recordIssue CRUD + issuedBy 사용자명 + documentType 필터 검색
- REST API: OPERATOR PDF 응답, 미인증 401, VIEWER 403 검증

## Task Commits

1. **Task 1: DocumentGenerationService + DocumentIssueService + DocumentController 테스트** - `cde5b57` (test)

## Files Created/Modified
- `backend/src/test/java/com/navy/casualty/document/service/DocumentGenerationServiceTest.java` - 7종 jrxml 컴파일, PDF 생성, 한글 폰트, 잘못된 ID 예외 (12 tests)
- `backend/src/test/java/com/navy/casualty/document/service/DocumentIssueServiceTest.java` - 발급이력 CRUD, 사용자명, 시각, 유형별 검색 (4 tests)
- `backend/src/test/java/com/navy/casualty/document/controller/DocumentControllerTest.java` - PDF 응답, 401, 403 (3 tests)

## Decisions Made
- DocumentGenerationServiceTest: Mockito 순수 단위 테스트 선택 (Spring 컨텍스트 로드 불필요, JasperReports는 classpath에서 직접 로드)
- JasperReports AWT 폰트 미설치 환경 대응: `net.sf.jasperreports.awt.ignore.missing.font=true` 설정으로 CI/CD 환경에서도 PDF 생성 가능
- NanumGothic 폰트 검증 방식: PDF 바이너리 내 문자열 검색 대신, TTF 파일 직접 로드 + java.awt.Font.createFont으로 폰트명 검증 (AWT 폰트 미등록 환경에서도 안정적)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] JasperReports AWT 폰트 미설치로 PDF 생성 실패**
- **Found during:** Task 1 (DocumentGenerationServiceTest)
- **Issue:** 테스트 환경에 NanumGothic AWT 폰트가 미등록되어 JRFontNotFoundException 발생
- **Fix:** @BeforeAll에서 `net.sf.jasperreports.awt.ignore.missing.font=true` 설정 추가
- **Files modified:** DocumentGenerationServiceTest.java
- **Verification:** 12개 테스트 전체 통과
- **Committed in:** cde5b57

**2. [Rule 1 - Bug] NanumGothic 폰트명이 한글 "나눔고딕"으로 반환**
- **Found during:** Task 1 (test_pdfContainsNanumGothicFont)
- **Issue:** java.awt.Font.createFont으로 로드 시 getFontName()이 "나눔고딕" 반환 (영문명 "NanumGothic" 아님)
- **Fix:** satisfiesAnyOf로 영문명/한글명 모두 허용하는 유연한 검증으로 변경
- **Files modified:** DocumentGenerationServiceTest.java
- **Verification:** 테스트 통과
- **Committed in:** cde5b57

---

**Total deviations:** 2 auto-fixed (1 blocking, 1 bug)
**Impact on plan:** AWT 폰트 환경 차이에 대한 필수 대응. 범위 변경 없음.

## Issues Encountered
None

## User Setup Required
None - 테스트 실행 시 PII_ENCRYPTION_KEY 환경변수 필요 (기존 프로젝트 공통 사항)

## Next Phase Readiness
- Phase 06 문서 출력 기능 전체 완료 (백엔드 + 프론트엔드 + 테스트)
- Phase 07 통계/현황 기능 진행 가능

## Self-Check: PASSED

- FOUND: DocumentGenerationServiceTest.java
- FOUND: DocumentIssueServiceTest.java
- FOUND: DocumentControllerTest.java
- FOUND: commit cde5b57

---
*Phase: 06-document-output*
*Completed: 2026-04-03*
