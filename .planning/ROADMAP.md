# Roadmap: 해군 사상자 관리 전산 시스템

**Created:** 2026-03-31
**Milestone:** v1.0
**Granularity:** Standard (5~8 phases)
**Mode:** YOLO

---

## Milestone: v1.0 — 해군 사상자 관리 전산 시스템 초도 운영

**Goal:** 사망자·상이자·전공사상심사 기록을 등록·조회·관리하고, 공식 문서 출력 및 통계 현황을 산출하는 시스템을 군 내부망에 배포한다.

**Success Criteria:**
- 해군 담당자가 사망자/상이자/심사 기록을 등록·수정·조회할 수 있다
- 국가유공자 확인서, 순직/사망확인서 등 6종 공식 문서를 인쇄할 수 있다
- 신분별·월별·연도별·부대별 통계 현황을 Excel로 내보낼 수 있다
- 모든 접근이 감사 로그에 기록되고 역할 기반 접근 제어가 동작한다
- Docker Compose 단일 명령으로 군 내부망 배포가 가능하다

---

## Phase 1: 프로젝트 기반 및 인프라 구성

**Goal:** 개발·배포 환경을 구성하고 DB 스키마와 공통 코드를 완성한다. 이후 모든 Phase가 의존하는 기반.

**Requirements:** INFRA-01~05

**Plans:** 4 plans

Plans:
- [x] 01-01-PLAN.md — Spring Boot 백엔드 뼈대 + Flyway 마이그레이션 V1~V9 + 공통 코드
- [x] 01-02-PLAN.md — React 프론트엔드 뼈대 + Ant Design Layout + 라우팅
- [x] 01-03-PLAN.md — Docker Compose 3종 + Dockerfile + 환경변수 템플릿
- [ ] 01-04-PLAN.md — UAT Gap Closure: admin BCrypt 해시 교정 + 401 리다이렉트 수정

**Key Deliverables:**
- Spring Boot 3.x + Java 21 프로젝트 뼈대 (패키지 구조, 공통 설정)
- React 18 + TypeScript + Ant Design 프론트엔드 뼈대 (Layout, 라우팅)
- PostgreSQL 16 DB 스키마 전체 (Flyway 마이그레이션 스크립트)
  - 사망자, 상이자, 전공사상심사, 감사로그, 문서발급이력, 사용자/역할 테이블
  - 계급·군구분·사망구분·보훈청 코드 테이블 + 초기 데이터
- Docker Compose 구성 (app + db)
- 공통 ApiResponse<T>, 전역 예외 처리, BaseAuditEntity

**Tech Decisions:**
- `@SQLRestriction("deleted_at IS NULL")` — soft delete 공통 패턴
- `spring-session-jdbc` — PostgreSQL 세션 저장소
- Flyway — DB 마이그레이션 도구
- Vite — 프론트엔드 빌드 도구 (에어갭 오프라인 번들)

---

## Phase 2: 인증·RBAC·감사 로그

**Goal:** 역할 기반 접근 제어, 세션 인증, 감사 로그 AOP를 완성한다. 보안 기반 없이는 이후 Phase 착수 불가.

**Requirements:** AUTH-01~07, AUDIT-01~06

**Plans:** 2/4 plans executed

Plans:
- [x] 02-01-PLAN.md — Security 인프라 + 인증 API (login/logout/me) + 계정 잠금
- [x] 02-02-PLAN.md — PII 암호화 (AES-256-GCM) + 마스킹 유틸 + AOP 감사 로그
- [x] 02-03-PLAN.md — 프론트엔드 인증 (로그인 화면 + 라우트 가드 + 관리자 화면)
- [x] 02-04-PLAN.md — 관리자 백엔드 API (사용자 CRUD + 감사 로그 검색)

**Key Deliverables:**
- 로그인/로그아웃 API + 로그인 화면 (Ant Design Form)
- Spring Security 세션 기반 인증 설정
  - 로그인 실패 5회 계정 잠금
  - 세션 타임아웃 30분, 동시 세션 1개 제한
  - 강제 로그아웃 API
- RBAC 구현: 4개 역할(ADMIN/MANAGER/OPERATOR/VIEWER) + @PreAuthorize
- AOP 감사 로그: 모든 서비스 메서드 진입/완료 시 자동 기록
- 주민번호 AES-256-GCM Hibernate AttributeConverter 구현
- 화면 마스킹 유틸: 역할별 주민번호 노출 수준 함수
- 관리자 화면: 사용자 관리, 역할 부여, 계정 잠금 해제
- 감사 로그 조회 화면 (ADMIN 전용)

**Tech Decisions:**
- `PiiEncryptionConverter` — Hibernate AttributeConverter + AES-256-GCM
- `AuditLogAspect` — @Around AOP로 감사 로그 자동 기록
- `JpaAuditingConfig` — @CreatedBy/@LastModifiedBy 자동 주입

---

## Phase 3: 사망자 관리

**Goal:** 사망자 정보의 등록·수정·삭제·조회와 Excel 내보내기를 완성한다.

**Requirements:** DEAD-01~07

**Plans:** 3/3 plans executed

Plans:
- [x] 03-01-PLAN.md — 백엔드 Dead 엔티티 + 코드 테이블 + QueryDSL Repository + Service + Controller
- [x] 03-02-PLAN.md — 프론트엔드 타입 + API 훅 + 목록 페이지 + 등록/수정/삭제 Modal
- [x] 03-03-PLAN.md — Excel 내보내기 (Apache POI + SXSSFWorkbook + 다운로드 버튼)

**Key Deliverables:**
- 사망자 CRUD REST API (QueryDSL 다중조건 검색)
  - 군번·성명·생년월일·계급·소속·사망구분 복합 필터
  - 논리 삭제 (삭제 사유 필수)
  - 중복 등록 방지 (군번 기준)
- 사망자 현황 목록 화면 (Ant Design Table, 서버사이드 페이징)
  - 검색 폼 접기/펼치기 (한국 행정 시스템 관례)
  - 조회 버튼 클릭 시 1페이지로 리셋
- 사망자 등록/수정 Modal (주민번호 체크섬 검증, 날짜 형식 YYYY.MM.DD)
- 사망 기록 상태 관리: 등록->확정->보훈청통보완료
- Apache POI SXSSFWorkbook Excel 내보내기 (주민번호 마스킹 적용)
- 심사 이력 연계: 전공사상심사 결과로 사망구분 자동 갱신

**Key Pattern:**
```
검색 폼(Form) + [조회] 버튼 -> 서버사이드 Table
등록 버튼 -> Modal -> POST API -> invalidateQueries -> 목록 자동 갱신
```

---

## Phase 4: 상이자 관리

**Goal:** 상이자 정보의 등록·수정·삭제·조회와 Excel 내보내기를 완성한다. Phase 3 패턴을 재활용한다.

**Requirements:** WOND-01~07

**Plans:** 3 plans

Plans:
- [x] 04-01-PLAN.md — 백엔드 Wounded 엔티티 + WoundedStatus(4단계) + WoundType(3-value enum) + VeteransOffice + QueryDSL Repository + Service + Controller
- [x] 04-02-PLAN.md — 프론트엔드 타입 + API 훅 + 목록 페이지 + 등록/수정/삭제 Modal (보훈청 Select + 상이구분)
- [ ] 04-03-PLAN.md — Excel 내보내기 (SXSSFWorkbook + 보훈청/병명/상이구분 컬럼 + 다운로드 버튼)

**Key Deliverables:**
- 상이자 CRUD REST API (Phase 3과 동일 패턴)
- 상이구분: 전공상/공상/일반상이 (boolean 아닌 3-value enum)
- 보훈청명 코드 테이블 연동 (전국 보훈청/보훈지청 Select)
- 상이 기록 상태 관리: 등록->심사중->확정->보훈청통보완료
- 상이자 현황 목록 화면 + 등록/수정 Modal
- Apache POI Excel 내보내기

**Note:** Phase 3 코드 패턴(Repository, Service, Controller, Table 컴포넌트)을 최대한 재활용하여 구현 속도 향상.

---

## Phase 5: 전공사상심사 관리

**Goal:** 전공사상심사 정보와 차수별 이력을 관리하고 보훈청 통보를 기록한다.

**Requirements:** REVW-01~08, AUDIT-07

**Plans:** 3 plans

Plans:
- [x] 05-01-PLAN.md — 백엔드 Review/ReviewHistory 엔티티 + ReviewClassification/ReviewStatus enum + Flyway V16 + QueryDSL Repository + Service(이력 스냅샷+자동반영+보훈청통보) + Controller
- [x] 05-02-PLAN.md — 프론트엔드 타입 + API 훅 + 목록 페이지 + 등록/수정/삭제 Modal + 이력 타임라인 Drawer
- [x] 05-03-PLAN.md — Excel 내보내기 (SXSSFWorkbook + 심사차수/분류/보훈청통보일 컬럼 + 다운로드 버튼)

**Key Deliverables:**
- 전공사상심사 CRUD REST API
- 심사 이력 Chain: `TB_REVIEW_HISTORY` — 심사차수별 변경 이력 완전 보존
  - 수정 시마다 변경 전 스냅샷을 이력 테이블에 자동 저장
- 심사 결과 분류: 전공상/공상/기각/보류
- 보훈청 통보 일시 기록 필드
- 심사 결과 -> 사망자/상이자 분류 자동 반영 로직
- 전공사상심사 현황 목록 화면 + 등록/수정 Modal
- 심사차수별 이력 조회 화면 (타임라인 UI)
- Excel 내보내기

---

## Phase 6: 공식 문서 출력

**Goal:** JasperReports 기반 공식 문서 7종 출력 기능을 완성한다.

**Requirements:** DOCU-01~09

**Plans:** 3 plans

Plans:
- [x] 06-01-PLAN.md — 백엔드 인프라 (JasperReports 의존성 + NanumGothic 폰트 + DocumentIssue 엔티티 + DocumentGenerationService + DocumentController + jrxml 7종)
- [x] 06-02-PLAN.md — 프론트엔드 (문서 출력 버튼 + 발급 목적 Modal + PDF 미리보기/인쇄 + 발급 이력 조회)
- [x] 06-03-PLAN.md — 통합 테스트 (JasperReports 렌더링 + 한글 폰트 검증 + REST API 테스트)

**Key Deliverables:**
- JasperReports 6.21.3 통합 설정 (iText 의존성 없는 LGPL 버전)
- 공식 문서 7종 JasperReports 템플릿(.jrxml):
  1. 국가유공자 요건 해당사실 확인서 (사망자용)
  2. 국가유공자 요건 해당사실 확인서 (상이자용)
  3. 전공사상심사결과서
  4. 순직/사망확인서
  5. 사망자 현황 보고서
  6. 상이자 현황 보고서
  7. 전사망자 확인증 발급대장
- `DocumentGenerationService` — 데이터 조회 + JasperReports 렌더링
- 발급 목적 입력 팝업 (출력 전 필수 입력) -> `TB_DOCUMENT_ISSUE` 자동 기록
- 인쇄 미리보기 (PDF iframe + 인쇄/다운로드 버튼)
- 문서 발급 이력 조회 화면

**Critical Pre-condition:** 해군 공식 양식 원본(PDF/Word) 입수 후 착수. 양식 확정 없이 시작 불가.

---

## Phase 7: 통계 및 현황

**Goal:** 신분별·월별·연도별·부대별 통계와 명부 조회 기능을 완성한다.

**Requirements:** STAT-01~07

**Plans:** 2 plans

Plans:
- [x] 07-01-PLAN.md — 백엔드 통계 API (DTO 5종 + QueryDSL 집계 Repository + Service + ExcelService + Controller + Flyway V17 인덱스)
- [x] 07-02-PLAN.md — 프론트엔드 통계 UI (타입 + API 훅 + 6개 페이지 + @ant-design/charts 바 차트 + 사이드바 메뉴 재구성 + 라우팅)

**Key Deliverables:**
- 통계 전용 Service 레이어 (QueryDSL GROUP BY 집계 쿼리)
  - @Transactional(readOnly = true)로 통계 쿼리 실행 (v1 단일 DataSource)
- 6종 통계 조회 API + 화면:
  1. 신분별 사망자 현황
  2. 월별 사망자 현황
  3. 연도별 사망자 현황
  4. 부대별 사망자 현황
  5. 부대별 사망자 명부
  6. 전사망자 명부
- 각 통계 화면: @ant-design/charts 바 차트 + Ant Design Table + Excel 다운로드 버튼
- 응답시간 5초 이내 보장 (PostgreSQL partial index 추가)
- 통계 Excel: SXSSFWorkbook 스트리밍 방식 (대용량 대비)

---

## Phase 8: 시스템 검증 및 배포 준비

**Goal:** 전체 기능 통합 검증, 보안 점검, 성능 테스트, 군 내부망 배포 패키징을 완성한다.

**Requirements:** 전 Phase 검증, INFRA-01~05 최종 확인

**Plans:** 7/8 plans executed

Plans:
- [x] 08-01-PLAN.md — 코드 품질 정리 + Flyway 마이그레이션 체인 검증 + 버전 관리 (SemVer + CHANGELOG)
- [x] 08-02-PLAN.md — 보안 강화 (TLS + 보안 헤더 + Actuator 제한) + 운영 프로파일 + Docker healthcheck + 부팅 검증
- [x] 08-03-PLAN.md — 초기 데이터 적재 (V18) + 비밀번호 변경 강제 (V19) + 데이터 검증 API
- [x] 08-04-PLAN.md — Excel 임포트 (사망자/상이자/심사) + 감사 로그 파티셔닝 + 월별 감사 보고서
- [x] 08-05-PLAN.md — 프론트엔드 최적화 (lazy route code splitting + 번들 분석 + 정적 자산 캐시)
- [x] 08-06-PLAN.md — 통합 테스트 7종 + Playwright E2E 3종 + OWASP ZAP + JMeter 성능 + 보안 체크리스트
- [x] 08-07-PLAN.md — 배포 패키징 (deploy.sh + generate-cert.sh + backup/restore.sh + package.sh + .env.example)
- [ ] 08-08-PLAN.md — 운영 문서 (배포 절차서 + 운영자 매뉴얼 + 교육 슬라이드)

**Key Deliverables:**
- 전체 기능 E2E 통합 테스트 (핵심 시나리오 기준)
- 보안 점검 체크리스트 검토:
  - 주민번호 암호화 확인
  - XSS/SQL Injection 방어 확인
  - HTTPS 설정 확인
  - 세션 보안 확인
- 성능 테스트: 동시 사용자 10명 기준 응답시간 측정
- Docker 이미지 오프라인 패키징 (tar 아카이브)
- 군 내부망 배포 절차서 작성
- 운영자 매뉴얼 작성
- 계급·부대·코드 초기 데이터 적재 (해군 담당자 제공 데이터 기준)

---

## Timeline Summary

| Phase | 내용 | 예상 기간 |
|-------|------|---------|
| 1 | 프로젝트 기반 + 인프라 | 1주 |
| 2 | 인증·RBAC·감사 로그 | 2주 |
| 3 | 사망자 관리 | 2주 |
| 4 | 상이자 관리 | 1.5주 |
| 5 | 전공사상심사 관리 | 1.5주 |
| 6 | 공식 문서 출력 | 3주 |
| 7 | 통계 및 현황 | 1.5주 |
| 8 | 검증 및 배포 준비 | 1주 |
| **합계** | | **약 14주** |

---

## Key Risks

| 위험 | 발생 Phase | 영향 | 대응 |
|------|-----------|------|------|
| 공식 양식 원본 입수 지연 | Phase 6 전 | 높음 — Phase 6 착수 불가 | Phase 1 시작과 동시에 양식 요청 |
| 군번·부대 코드 미제공 | Phase 1 | 중간 | Mock 데이터로 개발 진행, 실 데이터는 Phase 8에서 적재 |
| 에어갭 환경 Maven 의존성 문제 | Phase 1 | 높음 | 개발 초기 Nexus 미러 구성 선행 |
| 주민번호 암호화 후 검색 제약 | Phase 3~5 | 중간 | 생년월일 별도 컬럼 평문 저장으로 해결 |

---

## Architecture Overview

```
[React 18 + TypeScript + Ant Design]
    ↓ HTTPS (TLS)
[Spring Boot 3.x + Java 21]
├── Spring Security (세션 기반 RBAC)
├── JPA + QueryDSL (동적 검색)
├── JasperReports (문서 출력)
├── Apache POI (Excel 내보내기)
└── Hibernate AttributeConverter (PII 암호화)
    ↓
[PostgreSQL 16]
```

**배포:** Docker Compose (에어갭 군 내부망)

---
*Roadmap created: 2026-03-31*
*Based on: 6 research files (TECH-STACK, DOMAIN, ARCHITECTURE, UI-PATTERNS, LEGAL-REQUIREMENTS, FOREIGN-CASES)*
