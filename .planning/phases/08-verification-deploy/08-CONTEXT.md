# Phase 8: 시스템 검증 및 배포 준비 - Context

**Gathered:** 2026-04-04
**Status:** Ready for planning

<domain>
## Phase Boundary

전체 기능 통합 검증, 보안 점검, 성능 테스트, 군 내부망 배포 패키징을 완성한다.
Phase 1~7에서 구현된 모든 기능의 품질을 확인하고, 에어갭 환경에서 즉시 운영 가능한 상태로 만든다.

**포함:**
- E2E 통합 테스트 (API + Playwright)
- 보안 점검 (OWASP ZAP + 수동 체크리스트)
- 성능 테스트 (JMeter 동시 10명)
- Docker 오프라인 패키징 (tar 아카이브 + deploy.sh)
- TLS 설정 (Spring Boot 내장 + Nginx 선택적)
- 운영 문서 (배포 절차서 + 운영자 매뉴얼 + 교육 슬라이드)
- 백업/복원 스크립트
- 초기 데이터 적재 (Flyway V18+)
- Excel 임포트 기능 (기존 수작업 데이터 일괄 등록)
- 코드 품질 정리 (미사용 코드, 린트, 의존성)
- 감사 로그 파티셔닝 + 월별 감사 보고서
- 버전 관리 (SemVer + CHANGELOG.md)
- 운영 초기 세팅 (비밀번호 변경 강제 + 부팅 검증)

**미포함:** 새 기능 개발, v2 요구사항(STAT-08/09, PII-01/02, ATTC, FLOW, ESIG)

</domain>

<decisions>
## Implementation Decisions

### E2E 통합 테스트
- **D-01:** API 통합 테스트(`@SpringBootTest` + TestRestTemplate) + Playwright 브라우저 E2E 둘 다 실행
- **D-02:** 핵심 흐름 5~7개 시나리오 — 로그인, 사망자 CRUD, 상이자 CRUD, 심사 등록+자동반영, 문서 출력, 통계 조회, Excel 다운로드
- **D-03:** 보안 점검은 OWASP ZAP Docker 자동 스캔 + OWASP Top 10 수동 체크리스트 병행
- **D-04:** 성능 테스트는 JMeter 스크립트로 동시 사용자 10명 시나리오, STAT-07 응답시간 5초 기준 검증

### 배포 패키징
- **D-05:** `docker save`로 app + postgres:16-alpine 이미지 tar 아카이브 생성, `deploy.sh`가 `docker load` + `docker compose up` 자동 실행 (완전 자립형)
- **D-06:** 자체 서명 인증서 생성 스크립트(`generate-cert.sh`) 제공, 매뉴얼에 실 인증서 교체 방법 안내
- **D-07:** 향후 업데이트도 동일 패키징 패턴 반복 (중지→이미지 교체→재시작, Flyway 자동 마이그레이션)

### HTTPS/TLS
- **D-08:** Spring Boot 내장 TLS(`server.ssl.*` PKCS12) + 선택적 Nginx 리버스 프록시 구성 둘 다 지원
- **D-09:** HTTP→HTTPS 리다이렉트는 Spring Boot TomcatServletWebServerFactory에서 처리
- **D-10:** 필수 보안 헤더 적용 — HSTS, X-Content-Type-Options, X-Frame-Options, Secure/HttpOnly 쿠키 플래그

### 백업/복원
- **D-11:** `backup.sh` — pg_dump + 날짜별 파일명, 7일 롤링 보관(오래된 백업 자동 삭제), cron 등록 안내 포함
- **D-12:** `restore.sh` — `restore.sh <백업파일>`로 pg_restore 자동 실행, 복원 전 확인 프롬프트

### 모니터링/헬스체크
- **D-13:** Spring Actuator `/actuator/health`만 외부 노출, 나머지 비활성화
- **D-14:** Docker healthcheck — app: `/actuator/health` curl 체크, db: `pg_isready` 체크. docker-compose.prod.yml에 포함

### 운영 환경 프로파일
- **D-15:** HikariCP — maximumPoolSize=10, connectionTimeout=30s, idleTimeout=600s. application-prod.yml에 명시
- **D-16:** 로그 — root=WARN, com.navy.casualty=INFO, SQL/Hibernate=WARN. 파일 롤링 로그 일별, 30일 보관
- **D-17:** 세션 타임아웃 30분 + 동시 세션 1개를 application-prod.yml에 명시적 선언 (기본값 의존 방지)

### 에러 복구/장애 대응
- **D-18:** app + db 모두 `restart: unless-stopped` (수동 중지 존중, 비정상 종료 시 자동 재시작)
- **D-19:** HikariCP keepaliveTime=300s 설정으로 DB 연결 끊김 자동 복구
- **D-20:** 배포 절차서에 장애 판단 섹션 — healthcheck 실패 패턴, 주요 에러 로그 키워드(OOM, Connection refused, disk full), 대응 액션 매핑 테이블

### Flyway 마이그레이션 정리
- **D-21:** 현행 V1~V9, V13~V14 번호 유지, 운영 배포 전 `flyway validate` 실행으로 체인 정합성 확인
- **D-22:** V13(ssn_hash_column), V14(extend_code_tables) untracked 파일을 정식 커밋하여 운영 체인에 포함

### 프론트엔드 빌드 최적화
- **D-23:** `vite-plugin-visualizer`로 번들 분석 + 페이지별 lazy route code splitting 적용
- **D-24:** Vite 기본 해시 파일명(`[name].[hash].js`) + Spring Boot 정적 자산 `Cache-Control: max-age=31536000` 설정

### 초기 데이터 적재
- **D-25:** Flyway V18+ 마이그레이션으로 해군 실 코드 데이터 교체 (계급, 부대, 사망구분 기호 등). 운영은 신규 DB 전제 (Mock 전환 불필요)
- **D-26:** `/api/admin/data-check` 검증 API 엔드포인트 — 코드 테이블 건수, admin 계정 존재 여부 등 정합성 확인

### 운영 초기 세팅
- **D-27:** 첫 로그인 시 비밀번호 변경 강제 — TB_USER에 `password_changed` 플래그 추가, 초기값 false, false이면 비밀번호 변경 화면으로 리다이렉트
- **D-28:** ApplicationRunner 부팅 검증 — 환경변수(DB_PASSWORD, PII_ENCRYPTION_KEY, SSL 설정), 코드 테이블 건수, admin 계정 존재 여부 확인. 실패 시 로그 경고 출력 후 정상 기동

### 운영 문서
- **D-29:** Markdown 원본 + pandoc PDF 변환
- **D-30:** 배포 절차서 상세 범위 — 사전 요구사항, tar 로드/실행, TLS 설정, 환경변수, 헬스체크, 네트워크 구성도, 방화벽 규칙, 백업/복원 절차, 모니터링, 장애 판단, 트러블슈팅
- **D-31:** 운영자 매뉴얼 상세 범위 — 시스템 개요, 역할별 기능, 화면별 스크린샷, 업무 시나리오별 단계 가이드(사망자 등록→확인서 발급 흐름 등), 에러 대응 가이드
- **D-32:** 별도 교육용 슬라이드 — 전체 기능 상세 (모든 화면/기능 포함)

### 데이터 마이그레이션
- **D-33:** 관리자 화면에 Excel 임포트 기능 — 사망자/상이자/심사 데이터 일괄 등록 (기존 수작업 Excel에서 전환)
- **D-34:** 임포트 검증 — 필수 필드 + 주민번호 체크섬 형식 검증 + DB 중복 체크. 오류 행은 행번호 + 사유를 Excel 리포트로 반환

### 코드 품질/정리
- **D-35:** 미사용 코드/TODO/FIXME 자동 스캔 + 수동 정리
- **D-36:** Checkstyle(백엔드) + ESLint(프론트엔드) 규칙 강화 + 전체 코드베이스 재포맷
- **D-37:** 미사용 의존성 제거(build.gradle.kts, package.json) + 보안 취약점 버전 업데이트

### 감사 로그 아카이빙
- **D-38:** TB_AUDIT_LOG 연도별 파티셔닝, 5년 초과 파티션은 pg_dump 아카이브 + 별도 보관. 아카이브 스크립트 제공
- **D-39:** 월별 감사 보고서 자동 생성 — 매월 1일 전월 요약 PDF 생성 (접근 건수/작업유형별, 사용자별 활동, 주민번호 조회 건수, 삭제 이력 목록). JasperReports로 렌더링

### 버전/릴리스 관리
- **D-40:** SemVer v1.0.0 표기, 화면 하단 또는 로그인 페이지에 표시, application.yml에서 관리
- **D-41:** CHANGELOG.md — Git 태그 기준 버전별 변경사항 기록

### Claude's Discretion
- E2E 테스트 시나리오 세부 설계 (5~7개 핵심 흐름 내 세부 스텝)
- JMeter 시나리오 세부 구성 (API 엔드포인트 선정, 부하 패턴)
- OWASP ZAP 스캔 설정 (active/passive scan 범위)
- Nginx 리버스 프록시 설정 세부사항 (선택적 구성)
- 교육 슬라이드 레이아웃/디자인
- 감사 보고서 PDF 레이아웃
- 파티셔닝 DDL 세부사항 (range partition by year)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### 배포 인프라
- `Dockerfile` — 멀티스테이지 빌드 (JDK builder + Node frontend-builder + JRE runtime)
- `docker-compose.yml` — 기반 서비스 정의 (app + db)
- `docker-compose.prod.yml` — 운영 배포 설정 (TLS 환경변수 자리, DB 외부 포트 차단)
- `docker-compose.override.yml` — 로컬 개발용 설정

### 기존 테스트
- `backend/src/test/java/com/navy/casualty/` — 25개 단위 테스트 (도메인별 Service/Controller/Entity 테스트)

### 보안 구현
- `backend/src/main/java/com/navy/casualty/common/crypto/PiiEncryptionConverter.java` — AES-256-GCM 주민번호 암호화
- `backend/src/main/java/com/navy/casualty/common/crypto/RrnMaskingUtil.java` — 역할별 마스킹
- `backend/src/main/java/com/navy/casualty/common/config/SecurityConfig.java` — Spring Security RBAC 설정

### DB 마이그레이션
- `backend/src/main/resources/db/migration/V9__insert_mock_code_data.sql` — Mock 코드 데이터 (V18+에서 실 데이터로 교체)
- `backend/src/main/resources/db/migration/V6__create_audit_log_table.sql` — TB_AUDIT_LOG 스키마 (파티셔닝 대상)
- `backend/src/main/resources/db/migration/V13__add_ssn_hash_column.sql` — SSN 해시 컬럼 (untracked, 커밋 필요)
- `backend/src/main/resources/db/migration/V14__extend_code_tables.sql` — 코드 테이블 확장 (untracked, 커밋 필요)

### 문서 출력
- `backend/src/main/java/com/navy/casualty/document/service/DocumentGenerationService.java` — JasperReports 렌더링 서비스 (감사 보고서에 동일 패턴 활용)

### Excel 패턴
- `backend/src/main/java/com/navy/casualty/dead/service/DeadExcelService.java` — SXSSFWorkbook 스트리밍 패턴 (임포트 오류 리포트에 활용)

### 요구사항
- `.planning/REQUIREMENTS.md` — 전체 v1 요구사항 (검증 체크리스트 기준)
- `.planning/REQUIREMENTS.md` §감사 및 보안 AUDIT-03 — 감사 로그 5년 보관 의무

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `DocumentGenerationService`: JasperReports 렌더링 — 감사 보고서 PDF 생성에 동일 패턴 활용
- `DeadExcelService`: SXSSFWorkbook 스트리밍 — Excel 임포트 오류 리포트 생성에 활용
- `PiiEncryptionConverter`: AES-256-GCM — 임포트 시 주민번호 암호화에 기존 Converter 재활용
- `RrnMaskingUtil`: 역할별 마스킹 — 보안 점검 시 마스킹 동작 검증 대상
- 25개 단위 테스트: 도메인별 테스트 패턴 — 통합 테스트 확장 기반
- `Dockerfile`: 멀티스테이지 빌드 — tar 패키징의 소스 이미지

### Established Patterns
- Flyway V 순번 마이그레이션 — V18+로 실 데이터 적재
- `@AuditLog` AOP — 감사 로그 자동 기록 (보안 점검 검증 대상)
- `ApiResponse<T>` 래퍼 — data-check API에도 동일 응답 형식
- record DTO + from() 팩토리 — 검증 API 응답에도 동일 패턴
- React Router lazy route — 이미 라우팅 구조 존재, code splitting 적용점

### Integration Points
- `docker-compose.prod.yml` — TLS 환경변수 활성화, healthcheck 추가, restart 정책 변경
- `application-prod.yml` — HikariCP, 로그, 세션, Actuator 설정 추가
- `SecurityConfig.java` — 보안 헤더 설정, HTTP→HTTPS 리다이렉트 추가
- TB_USER — `password_changed` 컬럼 추가 (Flyway 마이그레이션)
- TB_AUDIT_LOG — 연도별 파티셔닝 적용 (Flyway 마이그레이션)
- 관리자 화면 — Excel 임포트 탭/버튼 추가

</code_context>

<specifics>
## Specific Ideas

- 배포 절차서에 네트워크 구성도, 방화벽 규칙까지 포함하는 상세 수준
- 운영자 매뉴얼에 화면별 스크린샷 + 업무 시나리오별 단계 가이드
- 교육 슬라이드는 전체 기능 상세 (모든 화면/기능 포함)
- 감사 보고서는 월별 자동 생성 (매월 1일 전월 요약)
- Excel 임포트 오류 시 행번호 + 사유를 Excel 리포트로 반환

</specifics>

<deferred>
## Deferred Ideas

- 읽기 전용 DB replica (v2 STAT-08)
- 배치 집계 테이블 (v2 STAT-09)
- 주민번호 클릭 시 노출 (v2 PII-01)
- 암호화 키 로테이션 (v2 PII-02)
- 첨부파일 관리 (v2 ATTC-01/02)
- 이의신청/승인 워크플로우 (v2 FLOW-01/02)
- PKI 전자서명 (v2 ESIG-01)
- DB 보안 강화 — pg_hba.conf 접근 제어, DB 사용자 권한 분리
- 재해 복구(DR) 계획 — RTO/RPO 목표, 이중화 구성

</deferred>

---

*Phase: 08-verification-deploy*
*Context gathered: 2026-04-04*
