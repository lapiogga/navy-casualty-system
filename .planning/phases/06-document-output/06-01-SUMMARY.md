---
phase: 06-document-output
plan: 01
subsystem: document
tags: [jasperreports, pdf, jrxml, nanumgothic, querydsl]

requires:
  - phase: 03-dead-management
    provides: Dead 엔티티/리포지토리/서비스
  - phase: 04-wounded-management
    provides: Wounded 엔티티/리포지토리/서비스
  - phase: 05-review-management
    provides: Review 엔티티/리포지토리/서비스
provides:
  - DocumentType 7종 enum
  - DocumentIssue 엔티티 + QueryDSL 리포지토리 + 서비스
  - DocumentGenerationService (JasperReports PDF 생성, .jrxml 캐싱)
  - DocumentController REST API (PDF 생성 + 발급 이력 조회)
  - 7종 .jrxml 프로토타입 템플릿 (단건형 4종 + 리스트형 3종)
  - NanumGothic 폰트 번들 + JasperReports 폰트 확장 등록
affects: [06-02, 06-03, frontend-document-pages]

tech-stack:
  added: [JasperReports 6.21.3, NanumGothic TTF]
  patterns: [ConcurrentHashMap 리포트 캐시, JRBeanCollectionDataSource 리스트 보고서, @PostConstruct 미리 컴파일]

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/document/enums/DocumentType.java
    - backend/src/main/java/com/navy/casualty/document/entity/DocumentIssue.java
    - backend/src/main/java/com/navy/casualty/document/service/DocumentGenerationService.java
    - backend/src/main/java/com/navy/casualty/document/service/DocumentIssueService.java
    - backend/src/main/java/com/navy/casualty/document/controller/DocumentController.java
    - backend/src/main/resources/reports/dead_certificate.jrxml
    - backend/src/main/resources/reports/wounded_certificate.jrxml
    - backend/src/main/resources/reports/review_result.jrxml
    - backend/src/main/resources/reports/death_confirmation.jrxml
    - backend/src/main/resources/reports/dead_status_report.jrxml
    - backend/src/main/resources/reports/wounded_status_report.jrxml
    - backend/src/main/resources/reports/issue_ledger.jrxml
    - backend/src/main/resources/fonts/NanumGothic.ttf
    - backend/src/main/resources/fonts/NanumGothicBold.ttf
    - backend/src/main/resources/fonts/fonts.xml
    - backend/src/main/resources/jasperreports_extension.properties
  modified:
    - backend/build.gradle.kts

key-decisions:
  - "JasperReports 6.21.3 LGPL 버전 사용 (iText 무의존)"
  - "ConcurrentHashMap으로 .jrxml 컴파일 결과 캐싱 (@PostConstruct 7종 미리 컴파일)"
  - "리스트형 보고서에 JRBeanCollectionDataSource + Map<String,Object> 행 변환 패턴"
  - "Content-Disposition filename*=UTF-8 인코딩으로 한글 파일명 깨짐 방지"

patterns-established:
  - "JasperReports 캐싱: ConcurrentHashMap<DocumentType, JasperReport> + computeIfAbsent"
  - "단건 문서: JREmptyDataSource + Parameter Map / 리스트 문서: JRBeanCollectionDataSource"
  - "문서 발급 시 이력 자동 기록 (DocumentIssueService.recordIssue)"

requirements-completed: [DOCU-01, DOCU-02, DOCU-03, DOCU-04, DOCU-05, DOCU-06, DOCU-07, DOCU-08]

duration: 10min
completed: 2026-04-04
---

# Phase 6 Plan 1: 문서 출력 백엔드 인프라 Summary

**JasperReports 6.21.3 기반 7종 공식 문서 PDF 생성 + NanumGothic 폰트 번들 + 발급 이력 자동 기록 REST API 완성**

## Performance

- **Duration:** 10min
- **Started:** 2026-04-03T14:55:57Z
- **Completed:** 2026-04-04T00:06:19Z
- **Tasks:** 2
- **Files modified:** 23

## Accomplishments
- JasperReports 6.21.3 의존성 추가 + NanumGothic Regular/Bold TTF 폰트 번들 + 폰트 확장 등록
- DocumentType 7종 enum + DocumentIssue 엔티티/QueryDSL 리포지토리/서비스 (발급 이력 CRUD)
- DocumentGenerationService: .jrxml @PostConstruct 미리 컴파일 + ConcurrentHashMap 캐시 + PDF byte[] 생성
- DocumentController: POST /{type}/generate (PDF 반환) + GET /issues (발급 이력 검색)
- 7종 .jrxml 프로토타입 템플릿 (단건형 4종: 확인서2, 심사결과서, 순직확인서 / 리스트형 3종: 사망자현황, 상이자현황, 발급대장)

## Task Commits

Each task was committed atomically:

1. **Task 1: JasperReports 의존성 + NanumGothic 폰트 + DocumentIssue** - `0e0bf64` (feat)
2. **Task 2: DocumentGenerationService + DocumentController + 7종 jrxml** - `68c89ee` (feat)

## Files Created/Modified
- `backend/build.gradle.kts` - JasperReports 6.21.3 의존성 추가
- `backend/src/main/java/com/navy/casualty/document/enums/DocumentType.java` - 문서 유형 7종 enum
- `backend/src/main/java/com/navy/casualty/document/entity/DocumentIssue.java` - 발급 이력 엔티티
- `backend/src/main/java/com/navy/casualty/document/repository/DocumentIssueRepository.java` - JPA 리포지토리
- `backend/src/main/java/com/navy/casualty/document/repository/DocumentIssueRepositoryCustom.java` - QueryDSL 인터페이스
- `backend/src/main/java/com/navy/casualty/document/repository/DocumentIssueRepositoryImpl.java` - QueryDSL 구현
- `backend/src/main/java/com/navy/casualty/document/dto/DocumentIssueRequest.java` - 발급 요청 DTO
- `backend/src/main/java/com/navy/casualty/document/dto/DocumentIssueResponse.java` - 발급 응답 DTO
- `backend/src/main/java/com/navy/casualty/document/dto/DocumentIssueSearchRequest.java` - 검색 DTO
- `backend/src/main/java/com/navy/casualty/document/service/DocumentIssueService.java` - 발급 이력 서비스
- `backend/src/main/java/com/navy/casualty/document/service/DocumentGenerationService.java` - PDF 생성 서비스
- `backend/src/main/java/com/navy/casualty/document/controller/DocumentController.java` - REST API
- `backend/src/main/resources/fonts/NanumGothic.ttf` - 한글 폰트 Regular
- `backend/src/main/resources/fonts/NanumGothicBold.ttf` - 한글 폰트 Bold
- `backend/src/main/resources/fonts/fonts.xml` - JasperReports 폰트 매핑
- `backend/src/main/resources/jasperreports_extension.properties` - 폰트 확장 등록
- `backend/src/main/resources/reports/dead_certificate.jrxml` - DOCU-01
- `backend/src/main/resources/reports/wounded_certificate.jrxml` - DOCU-02
- `backend/src/main/resources/reports/review_result.jrxml` - DOCU-03
- `backend/src/main/resources/reports/death_confirmation.jrxml` - DOCU-04
- `backend/src/main/resources/reports/dead_status_report.jrxml` - DOCU-05
- `backend/src/main/resources/reports/wounded_status_report.jrxml` - DOCU-06
- `backend/src/main/resources/reports/issue_ledger.jrxml` - DOCU-07

## Decisions Made
- JasperReports 6.21.3 LGPL 사용 (iText 별도 추가하지 않음)
- ConcurrentHashMap으로 .jrxml 컴파일 결과 캐싱 + @PostConstruct 미리 컴파일
- 리스트형 보고서에 Map<String,Object> 행 변환 + JRBeanCollectionDataSource 패턴
- Content-Disposition filename*=UTF-8 인코딩으로 한글 파일명 깨짐 방지

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Known Stubs
None - 모든 문서 유형에 대해 실제 엔티티 데이터를 조회하여 PDF를 생성한다. .jrxml 템플릿은 프로토타입 레이아웃이며 실제 해군 공식 양식 입수 시 .jrxml 파일만 교체하면 된다.

## Next Phase Readiness
- 백엔드 문서 생성 API 완성, 프론트엔드 연동 준비 완료 (06-02)
- 발급 이력 조회 API 완성, 프론트엔드 이력 페이지 구현 가능 (06-03)

## Self-Check: PASSED

All 10 key files verified present. Both task commits (0e0bf64, 68c89ee) verified in git log.

---
*Phase: 06-document-output*
*Completed: 2026-04-04*
