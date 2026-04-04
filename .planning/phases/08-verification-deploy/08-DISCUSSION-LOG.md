# Phase 8: 시스템 검증 및 배포 준비 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-04-04
**Phase:** 08-verification-deploy
**Areas discussed:** E2E 통합 테스트, 배포 패키징, 운영 문서, 초기 데이터 적재, HTTPS/TLS, 백업/복원, 모니터링/헬스체크, 운영 프로파일, Flyway 정리, 프론트엔드 빌드, 에러 복구/장애 대응, 운영 초기 세팅, 코드 품질, 데이터 마이그레이션, 사용자 교육, 버전/릴리스, 감사 로그 아카이빙

---

## E2E 통합 테스트

### 테스트 도구

| Option | Description | Selected |
|--------|-------------|----------|
| 백엔드 통합 테스트만 | @SpringBootTest + TestRestTemplate, 프론트엔드 E2E 생략 | |
| Playwright 브라우저 E2E | 로그인→CRUD→문서출력 전체 UI 흐름 | |
| 둘 다 | API 통합 테스트 + Playwright E2E | ✓ |

**User's choice:** 둘 다
**Notes:** 없음

### 시나리오 범위

| Option | Description | Selected |
|--------|-------------|----------|
| 핵심 흐름 5~7개 | 로그인, 사망자 CRUD, 상이자 CRUD, 심사+자동반영, 문서 출력, 통계, Excel | ✓ |
| 전체 기능 15개+ | 위 + 계정 잠금, 감사 로그, 역할별 접근 제어, 상태 전이, 중복 방지 등 | |
| 최소 3개 | 로그인, 사망자 등록+조회, 문서 출력 | |

**User's choice:** 핵심 흐름 5~7개

### 보안 점검

| Option | Description | Selected |
|--------|-------------|----------|
| 수동 체크리스트 | OWASP Top 10 수동 확인 + 문서화 | |
| OWASP ZAP 자동 스캔 | Docker ZAP 프록시 자동 취약점 스캔 | |
| 둘 다 | ZAP 자동 스캔 + 수동 체크리스트 | ✓ |

**User's choice:** 둘 다

### 성능 테스트

| Option | Description | Selected |
|--------|-------------|----------|
| JMeter | 동시 10명 시나리오, 응답시간 측정 + 리포트 | ✓ |
| 셸 스크립트 (ab/wrk) | 주요 API 부하 테스트 | |
| Gatling | Scala DSL, HTML 리포트 자동 생성 | |

**User's choice:** JMeter

---

## 배포 패키징

### 오프라인 패키징 구성

| Option | Description | Selected |
|--------|-------------|----------|
| docker save tar + deploy.sh | tar 아카이브 + 자동 load/up 스크립트 | ✓ |
| tar 아카이브만 | 이미지 tar + compose yml, 실행은 매뉴얼 참고 | |
| Helm chart | K8s 기반 배포 | |

**User's choice:** docker save tar + deploy.sh

### PostgreSQL 이미지 포함 여부

| Option | Description | Selected |
|--------|-------------|----------|
| 포함 | app + postgres:16-alpine 모두 tar에 포함 | ✓ |
| 제외 | app만, PostgreSQL은 기존 설치 사용 | |
| 선택적 | deploy.sh에서 로컬 PostgreSQL 확인 후 판단 | |

**User's choice:** 포함

### TLS 인증서 처리

| Option | Description | Selected |
|--------|-------------|----------|
| 자체 서명 인증서 생성 스크립트 | generate-cert.sh + 교체 안내 | ✓ |
| 매뉴얼에만 안내 | 보안팀 발급 인증서를 certs/에 배치 | |
| HTTP 전용 | TLS 생략 | |

**User's choice:** 자체 서명 인증서 생성 스크립트

---

## 운영 문서

### 문서 형식

| Option | Description | Selected |
|--------|-------------|----------|
| Markdown | docs/ 폴더, Git 관리 | |
| PDF | 공식 문서 형태 | |
| Markdown + PDF 변환 | Markdown 원본 + pandoc PDF | ✓ |

**User's choice:** Markdown + PDF 변환

### 배포 절차서 범위

| Option | Description | Selected |
|--------|-------------|----------|
| 표준 | 사전 요구사항, tar 로드/실행, TLS, 환경변수, 헬스체크, 트러블슈팅 | |
| 최소 | tar 로드 + compose up만 | |
| 상세 | 표준 + 네트워크 구성도, 방화벽, 백업/복원, 모니터링 | ✓ |

**User's choice:** 상세

### 운영자 매뉴얼 범위

| Option | Description | Selected |
|--------|-------------|----------|
| 표준 | 시스템 개요, 역할별 기능, 화면 사용법(텍스트), FAQ | |
| 상세 | 표준 + 스크린샷, 시나리오별 가이드, 에러 대응 | ✓ |
| 최소 | 로그인/로그아웃, 주요 기능 목록 | |

**User's choice:** 상세

---

## 초기 데이터 적재

### 실 코드 데이터 교체 방식

| Option | Description | Selected |
|--------|-------------|----------|
| Flyway 마이그레이션 | V18+ DELETE+INSERT, 자동 실행, 이력 추적 | ✓ |
| 별도 SQL 스크립트 | init-data.sql 수동 실행 | |
| CSV 임포트 유틸 | Excel/CSV 파싱 DB 적재 | |

**User's choice:** Flyway 마이그레이션

### 데이터 검증

| Option | Description | Selected |
|--------|-------------|----------|
| Flyway 후 검증 쿼리 | COUNT 검증 + 부팅 로그 | |
| 수동 SQL 확인 | SELECT 쿼리 목록 절차서 포함 | |
| 검증 API 엔드포인트 | /api/admin/data-check | ✓ |

**User's choice:** 검증 API 엔드포인트

### FK 참조 처리

| Option | Description | Selected |
|--------|-------------|----------|
| 운영 신규 DB 전제 | Mock 데이터 전환 불필요 | ✓ |
| Mock→실 데이터 매핑 | UPDATE 마이그레이션 | |
| 프로파일 분리 | 개발 Mock / 운영 실 데이터 | |

**User's choice:** 운영 신규 DB 전제

---

## HTTPS/TLS

### TLS 적용 방식

| Option | Description | Selected |
|--------|-------------|----------|
| Spring Boot 내장 TLS | server.ssl.* PKCS12 | |
| Nginx 리버스 프록시 | Nginx TLS 종단, Spring Boot HTTP | |
| 둘 다 지원 | 기본 내장 + 선택적 Nginx | ✓ |

**User's choice:** 둘 다 지원

### HTTP→HTTPS 리다이렉트

| Option | Description | Selected |
|--------|-------------|----------|
| Spring Boot 리다이렉트 | TomcatServletWebServerFactory 설정 | ✓ |
| 리다이렉트 없음 | HTTPS 포트만, HTTP 닫음 | |
| Nginx에서 처리 | Nginx 구성 시 Nginx 담당 | |

**User's choice:** Spring Boot 리다이렉트

### 보안 헤더

| Option | Description | Selected |
|--------|-------------|----------|
| 필수 보안 헤더만 | HSTS, X-Content-Type-Options, X-Frame-Options, Secure/HttpOnly | ✓ |
| 전체 보안 헤더 | + CSP, Referrer-Policy, Permissions-Policy | |
| Spring Security 기본값 | 커스텀 없이 기본만 | |

**User's choice:** 필수 보안 헤더만

---

## 백업/복원

### 백업 방식

| Option | Description | Selected |
|--------|-------------|----------|
| pg_dump 셸 스크립트 | backup.sh, 날짜별 파일명, cron 안내 | ✓ |
| Docker volume 스냅샷 | volume tar 백업 | |
| pg_basebackup | WAL 아카이빙 + PITR | |

**User's choice:** pg_dump 셸 스크립트

### 보관 정책

| Option | Description | Selected |
|--------|-------------|----------|
| 7일 롤링 | 최근 7일, 오래된 것 자동 삭제 | ✓ |
| 30일 롤링 | 최근 30일 보관 | |
| 무제한 | 삭제 없이 전부, 운영자 관리 | |

**User's choice:** 7일 롤링

### 복원 절차

| Option | Description | Selected |
|--------|-------------|----------|
| restore.sh 스크립트 | restore.sh <백업파일>, 확인 프롬프트 | ✓ |
| 절차서 수동 명령 | pg_restore 명령 절차서 기술 | |
| 복원 + 검증 자동화 | restore.sh + data-check API 호출 | |

**User's choice:** restore.sh 스크립트

---

## 모니터링/헬스체크

### Actuator 노출 범위

| Option | Description | Selected |
|--------|-------------|----------|
| 최소 (/health만) | 외부 노출 최소화 | ✓ |
| 운영 세트 | health + info + metrics | |
| 전체 | 모든 엔드포인트, ADMIN 인증 필수 | |

**User's choice:** /health만

### Docker healthcheck

| Option | Description | Selected |
|--------|-------------|----------|
| app + db 모두 | app: /actuator/health, db: pg_isready | ✓ |
| app만 | DB는 기본 상태 의존 | |
| 없음 | deploy.sh 수동 확인 | |

**User's choice:** app + db 모두

---

## 운영 프로파일

### 커넥션 풀

| Option | Description | Selected |
|--------|-------------|----------|
| HikariCP 소규모 튜닝 | maxPool=10, connTimeout=30s, idleTimeout=600s | ✓ |
| 기본값 그대로 | Spring Boot 기본 HikariCP | |
| 상세 튜닝 | leakDetection, validationTimeout 등까지 | |

**User's choice:** 소규모 튜닝

### 로그 레벨

| Option | Description | Selected |
|--------|-------------|----------|
| 운영 최적화 | root=WARN, app=INFO, SQL=WARN + 파일 롤링 일별 30일 | ✓ |
| 기본값 | INFO, 콘솔만 | |
| 상세 | app=DEBUG, SQL=DEBUG | |

**User's choice:** 운영 최적화

### 세션 설정

| Option | Description | Selected |
|--------|-------------|----------|
| prod에 동일값 명시 | 30분 + 동시 1개 명시적 선언 | ✓ |
| prod에서 값 변경 | 15분으로 강화 | |
| 기본 설정 의존 | application.yml 공통 | |

**User's choice:** prod에 동일값 명시

---

## Flyway 마이그레이션 정리

### 체인 정리 방식

| Option | Description | Selected |
|--------|-------------|----------|
| 현행 유지 + validate 검증 | V1~V9, V13~V14 번호 그대로, flyway validate 실행 | ✓ |
| V1부터 재정리 | 전체 V1~V12 재할당 | |
| V6 갭 메꾸기 | 빈 마이그레이션으로 연속 번호 유지 | |

**User's choice:** 현행 유지 + validate 검증

### V13/V14 처리

| Option | Description | Selected |
|--------|-------------|----------|
| 커밋 후 운영 체인 포함 | 정식 커밋, 운영 DB에서도 실행 | ✓ |
| 운영용 새 번호로 재작성 | V18+ 번호로 통합 재작성 | |
| 삭제 후 V1에 통합 | 초기 스키마 병합 (기존 DB 깨짐) | |

**User's choice:** 커밋 후 운영 체인 포함

---

## 프론트엔드 빌드 최적화

### 번들 최적화

| Option | Description | Selected |
|--------|-------------|----------|
| 분석 + 기본 최적화 | vite-plugin-visualizer + lazy route code splitting | ✓ |
| 현행 그대로 | Vite 기본 빌드 | |
| 고급 최적화 | manualChunks, tree-shaking 검증, gzip | |

**User's choice:** 분석 + 기본 최적화

### 캐싱 전략

| Option | Description | Selected |
|--------|-------------|----------|
| 해시 파일명 + Cache-Control | Vite [name].[hash].js + max-age=31536000 | ✓ |
| 캐시 없음 | no-cache | |
| ETag 기반 | 해시 없이 ETag | |

**User's choice:** 해시 파일명 + Cache-Control

---

## 에러 복구/장애 대응

### 재시작 정책

| Option | Description | Selected |
|--------|-------------|----------|
| unless-stopped | app + db 모두, 수동 중지 존중 | ✓ |
| 현행 유지 | app: always, db: no | |
| always | app + db 모두 항상 재시작 | |

**User's choice:** unless-stopped

### DB 연결 복구

| Option | Description | Selected |
|--------|-------------|----------|
| HikariCP 자동 복구 | keepaliveTime=300s, 자동 감지/교체 | ✓ |
| Spring Retry | 3회 재시도 + 지수 백오프 | |
| 별도 처리 없음 | HikariCP 기본값 | |

**User's choice:** HikariCP 자동 복구

### 장애 판단 가이드

| Option | Description | Selected |
|--------|-------------|----------|
| 절차서에 장애 판단 섹션 | 에러 키워드 + 대응 액션 테이블 | ✓ |
| 별도 트러블슈팅 문서 | 장애 유형별 독립 문서 | |
| 로그만 안내 | 파일 위치 + 확인 명령어 | |

**User's choice:** 절차서에 장애 판단 섹션

---

## 운영 초기 세팅

### 초기 비밀번호

| Option | Description | Selected |
|--------|-------------|----------|
| 첫 로그인 변경 강제 | password_changed 플래그, false이면 리다이렉트 | ✓ |
| 절차서 안내만 | 문구로 안내 | |
| 랜덤 비밀번호 생성 | deploy.sh에서 랜덤 생성 + 콘솔 출력 | |

**User's choice:** 첫 로그인 변경 강제

### 부팅 검증

| Option | Description | Selected |
|--------|-------------|----------|
| ApplicationRunner 검증 | 환경변수 + 코드 테이블 + admin 확인, 실패 시 경고 로그 | ✓ |
| 기동 차단 | 필수 설정 미충족 시 시작 거부 | |
| 체크 없음 | 런타임 에러로 발견 | |

**User's choice:** ApplicationRunner 검증

---

## 코드 품질/정리

### 미사용 코드 정리

| Option | Description | Selected |
|--------|-------------|----------|
| 자동 스캔 + 수동 정리 | 린트 도구 탐지 + TODO/FIXME 전수 조사 | ✓ |
| 린트 경고만 해결 | TODO 유지 | |
| 정리 안 함 | 현행 유지 | |

**User's choice:** 자동 스캔 + 수동 정리

### 린트/코드 스타일

| Option | Description | Selected |
|--------|-------------|----------|
| 기존 설정 점검 | 에러만 수정, 새 규칙 없음 | |
| 규칙 강화 + 전체 적용 | 코드 스타일 강화 후 전체 재포맷 | ✓ |
| 점검 안 함 | 이미 적용됨 | |

**User's choice:** 규칙 강화 + 전체 적용

### 의존성 정리

| Option | Description | Selected |
|--------|-------------|----------|
| 미사용 제거 + 버전 점검 | 미사용 탐지 제거, 취약점 버전 업데이트 | ✓ |
| 취약점만 점검 | npm audit + dependencyCheck | |
| 현행 유지 | 건드리지 않음 | |

**User's choice:** 미사용 제거 + 버전 점검

---

## 데이터 마이그레이션

### Excel 임포트

| Option | Description | Selected |
|--------|-------------|----------|
| 관리자 화면 Excel 임포트 | 업로드 버튼, 일괄 등록, 검증 오류 리포트 | ✓ |
| CLI 임포트 스크립트 | 서버 실행 Java/Python 스크립트 | |
| 불필요 | 건건이 수동 등록 | |

**User's choice:** 관리자 화면 Excel 임포트

### 임포트 검증

| Option | Description | Selected |
|--------|-------------|----------|
| 필수 + 형식 + 중복 | 주민번호 체크섬, DB 중복, 오류 행 Excel 리포트 | ✓ |
| 필수만 | 빈 값만 체크 | |
| 검증 없이 삽입 | DB 제약조건 의존 | |

**User's choice:** 필수 + 형식 + 중복

---

## 사용자 교육

### 교육 자료 형태

| Option | Description | Selected |
|--------|-------------|----------|
| 매뉴얼 겸용 | 운영자 매뉴얼이 교육 자료 | |
| 별도 교육 슬라이드 | PowerPoint/PDF 형태 | ✓ |
| 시연 시나리오 스크립트 | 핸즈온 스크립트 문서 | |

**User's choice:** 별도 교육 슬라이드

### 슬라이드 범위

| Option | Description | Selected |
|--------|-------------|----------|
| 개요 + 핵심 흐름 | 30분 내 교육 분량 | |
| 전체 기능 상세 | 모든 화면/기능 포함 | ✓ |
| 최소 소개 | 목적 + 로그인 수준 | |

**User's choice:** 전체 기능 상세

---

## 버전/릴리스 관리

### 버전 표기

| Option | Description | Selected |
|--------|-------------|----------|
| SemVer v1.0.0 | 화면 하단/로그인 표시, application.yml 관리 | ✓ |
| 날짜 기반 (2026.04) | 배포 연월 기준 | |
| 표기 없음 | 내부 시스템 생략 | |

**User's choice:** SemVer v1.0.0

### 릴리스 노트

| Option | Description | Selected |
|--------|-------------|----------|
| CHANGELOG.md | Git 태그 기준 버전별 기록 | ✓ |
| GitHub Release | Release 페이지 작성 | |
| 없음 | 커밋 로그 충분 | |

**User's choice:** CHANGELOG.md

### 업데이트 배포

| Option | Description | Selected |
|--------|-------------|----------|
| 동일 패키징 반복 | tar + deploy.sh 동일 패턴, Flyway 자동 마이그레이션 | ✓ |
| 경량 패치 | 변경분만 전달 | |
| 전체 재배포 | 매번 전체 이미지 재빌드 | |

**User's choice:** 동일 패키징 반복

---

## 감사 로그 아카이빙

### 5년 보관 전략

| Option | Description | Selected |
|--------|-------------|----------|
| 연도별 파티셔닝 + 아카이브 | 5년 초과 파티션 pg_dump 아카이브 | ✓ |
| 단일 테이블 유지 | 인덱스로 성능 관리 | |
| 외부 로그 시스템 | Elasticsearch 등 | |

**User's choice:** 연도별 파티셔닝 + 아카이브

### 정기 감사 보고서

| Option | Description | Selected |
|--------|-------------|----------|
| 감사 로그 조회 화면으로 충분 | 기존 ADMIN 화면 활용 | |
| 정기 보고서 자동 생성 | JasperReports 월별/분기별 PDF | ✓ |
| 별도 감사 대시보드 | 접근 통계, 이상 탐지 시각화 | |

**User's choice:** 정기 보고서 자동 생성

### 보고서 생성 주기/항목

| Option | Description | Selected |
|--------|-------------|----------|
| 월별 | 매월 1일 전월 요약, 접근 건수/작업유형별, 사용자별 활동, 주민번호 조회 건수, 삭제 이력 | ✓ |
| 분기별 | 3개월 단위 요약 | |
| 수동 | 관리자 기간 지정 | |

**User's choice:** 월별

---

## Claude's Discretion

- E2E 시나리오 세부 스텝 설계
- JMeter 시나리오 세부 구성
- OWASP ZAP 스캔 설정
- Nginx 리버스 프록시 세부사항
- 교육 슬라이드 레이아웃
- 감사 보고서 PDF 레이아웃
- 파티셔닝 DDL 세부사항

## Deferred Ideas

- DB 보안 강화 (pg_hba.conf, 사용자 권한 분리)
- 재해 복구(DR) 계획 (RTO/RPO, 이중화)
- v2 요구사항 전체 (STAT-08/09, PII-01/02, ATTC, FLOW, ESIG)
