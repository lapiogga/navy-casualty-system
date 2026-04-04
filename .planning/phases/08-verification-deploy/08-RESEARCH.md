# Phase 8: 시스템 검증 및 배포 준비 - Research

**Researched:** 2026-04-04
**Domain:** 통합 테스트, 보안 점검, 성능 테스트, Docker 오프라인 패키징, 운영 문서
**Confidence:** HIGH

## Summary

Phase 8은 Phase 1~7에서 구현된 전체 기능의 품질을 검증하고, 군 내부망(에어갭) 환경에서 즉시 운영 가능한 상태로 패키징하는 마지막 단계다. 핵심 영역은 (1) E2E 통합 테스트, (2) 보안 점검, (3) 성능 테스트, (4) Docker 오프라인 패키징, (5) 운영 문서, (6) 초기 데이터/코드 품질 정리로 나뉜다.

기존 코드베이스에 25개 단위 테스트, Spring Boot 3.5 + React 18 + Vite 8 + Antd 6 스택이 확립되어 있다. Dockerfile 멀티스테이지 빌드와 docker-compose.prod.yml 기본 골격이 이미 존재하므로, 이를 확장하는 방식으로 진행한다. 새로운 기능 개발은 Excel 임포트, 감사 보고서 월별 자동생성, 비밀번호 변경 강제, 부팅 검증 정도에 한정된다.

**Primary recommendation:** 작업을 6개 이상의 Plan으로 분리하되 — (1) 코드 품질/인프라 정리, (2) 보안 강화 + TLS + 보안 헤더, (3) 초기 데이터 + Excel 임포트 + 감사 아카이빙, (4) 통합/E2E 테스트, (5) 성능 테스트 + 프론트엔드 최적화, (6) 배포 패키징 + 운영 문서 — 순서로 진행하라.

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions
- **D-01:** API 통합 테스트(`@SpringBootTest` + TestRestTemplate) + Playwright 브라우저 E2E 둘 다 실행
- **D-02:** 핵심 흐름 5~7개 시나리오 — 로그인, 사망자 CRUD, 상이자 CRUD, 심사 등록+자동반영, 문서 출력, 통계 조회, Excel 다운로드
- **D-03:** 보안 점검은 OWASP ZAP Docker 자동 스캔 + OWASP Top 10 수동 체크리스트 병행
- **D-04:** 성능 테스트는 JMeter 스크립트로 동시 사용자 10명 시나리오, STAT-07 응답시간 5초 기준 검증
- **D-05:** `docker save`로 app + postgres:16-alpine 이미지 tar 아카이브 생성, `deploy.sh`가 `docker load` + `docker compose up` 자동 실행
- **D-06:** 자체 서명 인증서 생성 스크립트(`generate-cert.sh`) 제공
- **D-07:** 향후 업데이트도 동일 패키징 패턴
- **D-08:** Spring Boot 내장 TLS(`server.ssl.*` PKCS12) + 선택적 Nginx 리버스 프록시
- **D-09:** HTTP->HTTPS 리다이렉트는 Spring Boot TomcatServletWebServerFactory
- **D-10:** 필수 보안 헤더 — HSTS, X-Content-Type-Options, X-Frame-Options, Secure/HttpOnly 쿠키
- **D-11:** `backup.sh` — pg_dump + 7일 롤링 보관
- **D-12:** `restore.sh` — pg_restore 자동 실행
- **D-13:** Spring Actuator `/actuator/health`만 외부 노출
- **D-14:** Docker healthcheck — app: `/actuator/health`, db: `pg_isready`
- **D-15:** HikariCP — maximumPoolSize=10, connectionTimeout=30s, idleTimeout=600s
- **D-16:** 로그 — root=WARN, com.navy.casualty=INFO, 파일 롤링 일별 30일 보관
- **D-17:** 세션 타임아웃 30분 + 동시 세션 1개 명시적 선언
- **D-18:** app + db 모두 `restart: unless-stopped`
- **D-19:** HikariCP keepaliveTime=300s
- **D-20:** 배포 절차서에 장애 판단 섹션
- **D-21:** 현행 V1~V17 번호 유지, `flyway validate` 실행
- **D-22:** V13, V14 untracked 파일 정식 커밋
- **D-23:** `vite-plugin-visualizer` + lazy route code splitting
- **D-24:** Vite 해시 파일명 + Spring Boot Cache-Control: max-age=31536000
- **D-25:** Flyway V18+로 해군 실 코드 데이터 교체
- **D-26:** `/api/admin/data-check` 검증 API
- **D-27:** 첫 로그인 시 비밀번호 변경 강제 (`password_changed` 플래그)
- **D-28:** ApplicationRunner 부팅 검증
- **D-29:** Markdown + pandoc PDF 변환
- **D-30:** 배포 절차서 상세 범위
- **D-31:** 운영자 매뉴얼 상세 범위
- **D-32:** 별도 교육용 슬라이드
- **D-33:** 관리자 화면 Excel 임포트 (사망자/상이자/심사)
- **D-34:** 임포트 검증 — 필수 필드 + 주민번호 체크섬 + DB 중복 체크
- **D-35:** 미사용 코드/TODO/FIXME 스캔 + 정리
- **D-36:** Checkstyle + ESLint 규칙 강화 + 재포맷
- **D-37:** 미사용 의존성 제거 + 보안 취약점 버전 업데이트
- **D-38:** TB_AUDIT_LOG 연도별 파티셔닝 + 5년 초과 아카이브 스크립트
- **D-39:** 월별 감사 보고서 자동 생성 (JasperReports)
- **D-40:** SemVer v1.0.0 표기
- **D-41:** CHANGELOG.md

### Claude's Discretion
- E2E 테스트 시나리오 세부 설계
- JMeter 시나리오 세부 구성
- OWASP ZAP 스캔 설정
- Nginx 리버스 프록시 설정 세부사항
- 교육 슬라이드 레이아웃/디자인
- 감사 보고서 PDF 레이아웃
- 파티셔닝 DDL 세부사항

### Deferred Ideas (OUT OF SCOPE)
- 읽기 전용 DB replica (v2 STAT-08)
- 배치 집계 테이블 (v2 STAT-09)
- 주민번호 클릭 시 노출 (v2 PII-01)
- 암호화 키 로테이션 (v2 PII-02)
- 첨부파일 관리 (v2 ATTC-01/02)
- 이의신청/승인 워크플로우 (v2 FLOW-01/02)
- PKI 전자서명 (v2 ESIG-01)
- DB 보안 강화 (pg_hba.conf, 권한 분리)
- 재해 복구(DR) 계획
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| INFRA-01 | Docker Compose 단일 명령 배포 | deploy.sh + docker-compose.prod.yml + tar 아카이브 패턴 |
| INFRA-02 | PostgreSQL 16 + Flyway 마이그레이션 | V13/V14 커밋 + V18+ 실 데이터 + flyway validate |
| INFRA-03 | spring-session-jdbc 세션 저장소 | application-prod.yml 세션 설정 명시적 선언 |
| INFRA-04 | 오프라인 의존성 해결 | mavenLocal + npm ci --offline + tar 패키징 |
| INFRA-05 | HTTPS/TLS 전송 암호화 | PKCS12 keystore + generate-cert.sh + 보안 헤더 |
| AUTH-01~07 | 전체 인증/접근 제어 | E2E 테스트 시나리오에서 검증 |
| AUDIT-01~07 | 전체 감사/보안 | OWASP ZAP 스캔 + 수동 체크리스트 + 감사 로그 파티셔닝 |
| DEAD-01~07 | 사망자 관리 전체 | E2E CRUD 시나리오 + Excel 임포트 |
| WOND-01~07 | 상이자 관리 전체 | E2E CRUD 시나리오 + Excel 임포트 |
| REVW-01~08 | 전공사상심사 전체 | E2E 심사+자동반영 시나리오 + Excel 임포트 |
| DOCU-01~09 | 문서 출력 전체 | E2E 문서 출력 시나리오 |
| STAT-01~07 | 통계 전체 | E2E 통계 조회 + JMeter 성능 검증 (5초 기준) |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- 모든 응답/주석 한국어, 식별자/변수명 영어
- 코드 주석 한국어
- 간결한 코드, 불필요한 추상화 금지
- 기존 코드 스타일/패턴 따르기
- 보안 취약점 주의 (인젝션, XSS)
- 파일 800줄, 함수 50줄 한계
- 시크릿 환경변수화 필수
- zod/스키마 입력 검증
- immutability 원칙
- TDD: RED -> GREEN -> IMPROVE

## Standard Stack

### Core (이미 확립됨)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Spring Boot | 3.5.13 | 백엔드 프레임워크 | 프로젝트 기존 스택 |
| React | 18.3.1 | 프론트엔드 프레임워크 | 프로젝트 기존 스택 |
| Vite | 8.0.3 | 빌드 도구 | 프로젝트 기존 스택 |
| PostgreSQL | 16-alpine | 데이터베이스 | 프로젝트 기존 스택 |
| Flyway | (Spring Boot managed) | DB 마이그레이션 | 프로젝트 기존 스택 |
| JasperReports | 6.21.3 | PDF 렌더링 | 프로젝트 기존 스택 (감사 보고서에 재활용) |
| Apache POI | 5.3.0 | Excel 처리 | 프로젝트 기존 스택 (임포트에 재활용) |

### Phase 8 신규 추가
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| @playwright/test | 1.59.1 | 브라우저 E2E 테스트 | D-01 E2E 시나리오 |
| rollup-plugin-visualizer | 7.0.1 | 번들 분석 | D-23 번들 최적화 |
| OWASP ZAP (Docker) | latest stable | 보안 자동 스캔 | D-03 보안 점검 |
| Apache JMeter | 5.6.3 | 성능 테스트 | D-04 동시 사용자 테스트 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| JMeter | k6 / Gatling | JMeter는 GUI 시나리오 편집 용이 + JMX 스크립트 재사용성. 결정 잠금 (D-04) |
| Playwright | Cypress | Playwright가 멀티브라우저 지원 + 더 빠름. npx로 이미 사용 가능 |
| rollup-plugin-visualizer | vite-bundle-visualizer | rollup-plugin-visualizer가 Vite 8과 호환성 더 안정적 |

**Installation (프론트엔드):**
```bash
cd frontend
npm install -D @playwright/test rollup-plugin-visualizer
npx playwright install chromium
```

**JMeter:**
JMeter는 시스템에 미설치. Docker 이미지 `justb4/jmeter:5.6.3` 사용 또는 로컬 설치 필요. JMX 스크립트만 작성하고 실행은 Docker로 가능.

## Architecture Patterns

### 추가 디렉토리 구조
```
project-root/
├── deploy/                        # 배포 패키징 스크립트
│   ├── deploy.sh                  # docker load + compose up 자동 실행
│   ├── generate-cert.sh           # 자체 서명 PKCS12 인증서 생성
│   ├── backup.sh                  # pg_dump 백업 (7일 롤링)
│   ├── restore.sh                 # pg_restore 복원
│   └── archive-audit-log.sh       # 감사 로그 아카이브 스크립트
├── docs/                          # 운영 문서 (Markdown 원본)
│   ├── deployment-guide.md        # 배포 절차서
│   ├── operations-manual.md       # 운영자 매뉴얼
│   └── training-slides.md         # 교육 슬라이드 (Marp/pandoc beamer)
├── test/
│   ├── e2e/                       # Playwright E2E 테스트
│   │   ├── playwright.config.ts
│   │   └── specs/
│   ├── integration/               # API 통합 테스트 (백엔드 test/ 내)
│   └── performance/               # JMeter JMX 스크립트
│       └── load-test.jmx
├── docker-compose.yml
├── docker-compose.prod.yml
└── CHANGELOG.md
```

### Pattern 1: Spring Boot 통합 테스트 (@SpringBootTest)
**What:** 실제 Spring 컨텍스트 + 내장 PostgreSQL로 API 엔드포인트 E2E 검증
**When to use:** D-01 API 통합 테스트
**Example:**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeadApiIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void 사망자_CRUD_전체_흐름() {
        // 로그인
        var loginResp = restTemplate.postForEntity("/api/auth/login", loginDto, ApiResponse.class);
        assertThat(loginResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        // CRUD 연쇄 테스트...
    }
}
```

### Pattern 2: Playwright E2E
**What:** 브라우저 기반 사용자 흐름 자동화
**When to use:** D-01/D-02 핵심 시나리오 검증
**Example:**
```typescript
// test/e2e/specs/login.spec.ts
import { test, expect } from '@playwright/test';

test('로그인 후 사망자 목록 조회', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[data-testid="username"]', 'admin');
  await page.fill('[data-testid="password"]', 'admin123');
  await page.click('[data-testid="login-btn"]');
  await expect(page).toHaveURL('/dead');
  await expect(page.locator('table')).toBeVisible();
});
```

### Pattern 3: Docker 오프라인 패키징
**What:** 에어갭 환경용 tar 아카이브 생성 + 자립형 배포 스크립트
**When to use:** D-05/D-07
**Example:**
```bash
#!/bin/bash
# package.sh - 오프라인 배포 패키지 생성
docker compose -f docker-compose.yml -f docker-compose.prod.yml build
docker save navy-casualty-app:latest postgres:16-alpine | gzip > navy-casualty-offline.tar.gz
# deploy.sh, docker-compose 파일, 환경변수 템플릿 등 함께 아카이브
tar czf navy-casualty-deploy-v1.0.0.tar.gz \
  navy-casualty-offline.tar.gz \
  docker-compose.yml docker-compose.prod.yml \
  deploy.sh generate-cert.sh backup.sh restore.sh \
  .env.example
```

### Pattern 4: HTTP->HTTPS 리다이렉트
**What:** Spring Boot TomcatServletWebServerFactory로 HTTP 요청을 HTTPS로 강제 전환
**When to use:** D-09
**Example:**
```java
@Bean
public ServletWebServerFactory servletContainer() {
    TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
        @Override
        protected void postProcessContext(Context context) {
            var constraint = new SecurityConstraint();
            constraint.setUserConstraint("CONFIDENTIAL");
            var collection = new SecurityCollection();
            collection.addPattern("/*");
            constraint.addCollection(collection);
            context.addConstraint(constraint);
        }
    };
    tomcat.addAdditionalTomcatConnectors(httpConnector());
    return tomcat;
}

private Connector httpConnector() {
    var connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
    connector.setScheme("http");
    connector.setPort(8080);
    connector.setSecure(false);
    connector.setRedirectPort(8443);
    return connector;
}
```

### Pattern 5: 보안 헤더 설정
**What:** Spring Security에서 HSTS, X-Content-Type-Options 등 보안 헤더 적용
**When to use:** D-10
**Example:**
```java
// SecurityConfig.java filterChain에 추가
http.headers(headers -> headers
    .httpStrictTransportSecurity(hsts -> hsts
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000))
    .contentTypeOptions(Customizer.withDefaults())
    .frameOptions(frame -> frame.deny())
);
```

### Pattern 6: ApplicationRunner 부팅 검증
**What:** 앱 시작 시 필수 환경변수, 코드 테이블 건수, admin 계정 등 확인
**When to use:** D-28
**Example:**
```java
@Component
@RequiredArgsConstructor
public class StartupValidator implements ApplicationRunner {
    private final CodeTableRepository codeTableRepo;
    private final UserRepository userRepo;

    @Override
    public void run(ApplicationArguments args) {
        // 환경변수 검증
        checkEnv("DB_PASSWORD");
        checkEnv("PII_ENCRYPTION_KEY");
        // 코드 테이블 건수 검증
        long codeCount = codeTableRepo.count();
        if (codeCount == 0) log.warn("코드 테이블이 비어 있습니다");
        // admin 계정 확인
        if (!userRepo.existsByUsername("admin")) log.warn("admin 계정이 없습니다");
    }
}
```

### Pattern 7: Lazy Route Code Splitting
**What:** React Router lazy()로 페이지별 코드 분리
**When to use:** D-23
**Example:**
```typescript
import { lazy } from 'react';
const DeadListPage = lazy(() => import('../pages/dead/DeadListPage'));
const WoundedListPage = lazy(() => import('../pages/wounded/WoundedListPage'));
// routes/index.tsx에서 lazy import로 교체
```

### Pattern 8: 감사 로그 연도별 파티셔닝
**What:** PostgreSQL RANGE 파티셔닝으로 TB_AUDIT_LOG를 연도별 분리
**When to use:** D-38
**Example:**
```sql
-- Flyway 마이그레이션: 기존 테이블을 파티션 테이블로 전환
-- 주의: 기존 데이터가 있으면 마이그레이션 스크립트에서 데이터 이동 필요
ALTER TABLE tb_audit_log RENAME TO tb_audit_log_old;

CREATE TABLE tb_audit_log (
    LIKE tb_audit_log_old INCLUDING ALL
) PARTITION BY RANGE (created_at);

CREATE TABLE tb_audit_log_2026 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
CREATE TABLE tb_audit_log_2027 PARTITION OF tb_audit_log
    FOR VALUES FROM ('2027-01-01') TO ('2028-01-01');
-- 기존 데이터 이동
INSERT INTO tb_audit_log SELECT * FROM tb_audit_log_old;
DROP TABLE tb_audit_log_old;
```

### Anti-Patterns to Avoid
- **테스트 DB 공유:** 통합 테스트에서 실 DB 사용 금지 — `@SpringBootTest` + H2 또는 Testcontainers 사용
- **tar 내부에 .env 파일 포함:** 시크릿이 패키지에 포함되면 안 됨 — `.env.example` 템플릿만 포함
- **Flyway 마이그레이션에서 DROP 남용:** 운영 데이터 보호를 위해 RENAME + 새 테이블 생성 패턴 사용
- **Playwright 테스트에서 하드코딩 URL:** baseURL은 playwright.config.ts에서 환경변수로 관리

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 보안 취약점 스캔 | 수동 체크만 | OWASP ZAP Docker + 수동 체크리스트 병행 | 자동화된 스캔이 알려진 취약점 패턴을 빠짐없이 검사 |
| 부하 테스트 | 직접 curl 루프 | JMeter JMX 스크립트 | 동시성, 응답시간 통계, 보고서 자동 생성 |
| TLS 인증서 | 수동 openssl 명령 나열 | generate-cert.sh 스크립트화 | 재현 가능성, 오류 방지 |
| 번들 분석 | 수동 빌드 출력 검토 | rollup-plugin-visualizer | 시각적 번들 구성, 대형 의존성 즉시 식별 |
| PDF 변환 | 커스텀 PDF 생성 | pandoc (Markdown -> PDF) | 문서 원본은 Markdown, PDF는 빌드 산출물 |
| 감사 보고서 PDF | 커스텀 HTML-PDF | JasperReports (기존 패턴) | DocumentGenerationService 패턴 100% 재활용 |
| DB 백업/복원 | 커스텀 스크립트 로직 | pg_dump/pg_restore wrapper | 검증된 도구, 데이터 무결성 보장 |

## Common Pitfalls

### Pitfall 1: Flyway 파티셔닝 마이그레이션 실패
**What goes wrong:** 기존 TB_AUDIT_LOG에 외래키나 인덱스가 걸려 있으면 RENAME + 재생성이 실패한다.
**Why it happens:** 파티션 테이블은 일반 테이블과 제약조건 처리가 다르다.
**How to avoid:** 마이그레이션 전 기존 제약조건/인덱스를 DROP 후 재생성. TB_AUDIT_LOG는 append-only 설계이므로 외래키가 없어야 정상.
**Warning signs:** `flyway validate` 실패, 파티션 생성 시 "cannot create partition" 오류.

### Pitfall 2: Docker save/load 이미지 태그 누락
**What goes wrong:** `docker save`로 저장한 이미지를 `docker load`했을 때 태그가 `<none>:<none>`으로 나타남.
**Why it happens:** 이미지 이름을 명시하지 않고 ID로 저장한 경우.
**How to avoid:** `docker save navy-casualty-app:v1.0.0 postgres:16-alpine -o images.tar` — 반드시 이름:태그 명시.
**Warning signs:** `docker images`에서 태그가 `<none>`으로 표시.

### Pitfall 3: Playwright 테스트에서 Spring Boot 미기동
**What goes wrong:** E2E 테스트 실행 시 백엔드가 아직 준비되지 않아 연결 실패.
**Why it happens:** Playwright 테스트와 Spring Boot가 별도 프로세스.
**How to avoid:** `playwright.config.ts`의 `webServer` 옵션으로 Spring Boot를 자동 시작하거나, Docker Compose 기반 테스트 환경 구성.
**Warning signs:** `ECONNREFUSED` 에러.

### Pitfall 4: PKCS12 keystore 비밀번호 노출
**What goes wrong:** keystore 비밀번호가 docker-compose.yml에 하드코딩됨.
**Why it happens:** 빠른 구현 시 환경변수화를 잊음.
**How to avoid:** `SSL_KEYSTORE_PASSWORD` 환경변수로만 전달. `.env.example`에 플레이스홀더만 포함.
**Warning signs:** git diff에 평문 비밀번호 노출.

### Pitfall 5: Excel 임포트 대량 데이터 OOM
**What goes wrong:** 수천 행 Excel 파일 임포트 시 메모리 부족.
**Why it happens:** POI의 XSSF(DOM 기반)가 전체 파일을 메모리에 로드.
**How to avoid:** 임포트에는 `StreamingReader` 또는 SAX 기반 파서 사용. 기존 내보내기는 SXSSFWorkbook이지만 읽기에는 별도 전략 필요.
**Warning signs:** 대량 파일에서 `java.lang.OutOfMemoryError`.

### Pitfall 6: 감사 로그 파티셔닝 후 JPA 쿼리 실패
**What goes wrong:** JPA 엔티티가 파티션 테이블과 매핑 실패.
**Why it happens:** 파티션 상위 테이블과 JPA @Table 매핑은 정상 작동하지만, INSERT 시 파티션 키(created_at)가 null이면 오류.
**How to avoid:** `@PrePersist`로 created_at 자동 설정 확인. BaseAuditEntity에서 이미 처리하고 있을 가능성 높음 — 검증 필요.
**Warning signs:** `no partition found for row` 에러.

### Pitfall 7: application-prod.yml 설정 병합
**What goes wrong:** prod 프로파일 설정이 기본 application.yml과 충돌.
**Why it happens:** Spring Boot 프로파일 설정 병합 규칙 오해.
**How to avoid:** application-prod.yml에는 오버라이드할 값만 명시. 기존 HikariCP maximumPoolSize=20(기본)을 10으로 변경하는 것이므로 prod에서 명시적 재정의.

## Code Examples

### generate-cert.sh
```bash
#!/bin/bash
# 자체 서명 PKCS12 인증서 생성
CERT_DIR="./certs"
mkdir -p "$CERT_DIR"

# 인증서 정보
CN="${1:-navy-casualty.local}"
VALIDITY=365

openssl req -x509 -newkey rsa:2048 -nodes \
  -keyout "$CERT_DIR/server.key" \
  -out "$CERT_DIR/server.crt" \
  -days $VALIDITY \
  -subj "/CN=$CN"

openssl pkcs12 -export \
  -in "$CERT_DIR/server.crt" \
  -inkey "$CERT_DIR/server.key" \
  -out "$CERT_DIR/keystore.p12" \
  -name server \
  -passout "pass:${SSL_KEYSTORE_PASSWORD:-changeit}"

echo "인증서 생성 완료: $CERT_DIR/keystore.p12"
echo "운영 환경에서는 실 인증서로 교체하세요."
```

### backup.sh
```bash
#!/bin/bash
# pg_dump 백업 + 7일 롤링 보관
BACKUP_DIR="${BACKUP_DIR:-./backups}"
DB_CONTAINER="${DB_CONTAINER:-navy-casualty-db-1}"
RETENTION_DAYS=7

mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/navy_casualty_$TIMESTAMP.sql.gz"

docker exec "$DB_CONTAINER" pg_dump -U navy_user navy_casualty | gzip > "$BACKUP_FILE"

if [ $? -eq 0 ]; then
  echo "백업 완료: $BACKUP_FILE"
  # 7일 이상 된 백업 삭제
  find "$BACKUP_DIR" -name "navy_casualty_*.sql.gz" -mtime +$RETENTION_DAYS -delete
  echo "$(find "$BACKUP_DIR" -name "*.sql.gz" | wc -l)개 백업 보관 중"
else
  echo "백업 실패!" >&2
  exit 1
fi
```

### deploy.sh
```bash
#!/bin/bash
set -e
echo "=== 해군 사상자 관리 시스템 배포 ==="

# 이미지 로드
echo "1. Docker 이미지 로드 중..."
docker load -i navy-casualty-offline.tar.gz

# 환경변수 확인
if [ ! -f .env ]; then
  echo ".env 파일이 없습니다. .env.example을 복사하여 설정하세요."
  exit 1
fi

# 기존 컨테이너 중지 (업데이트 시)
docker compose -f docker-compose.yml -f docker-compose.prod.yml down 2>/dev/null || true

# 서비스 시작
echo "2. 서비스 시작 중..."
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

echo "3. 헬스체크 대기 중..."
for i in $(seq 1 30); do
  if curl -sf -k https://localhost:8443/actuator/health > /dev/null 2>&1; then
    echo "시스템 정상 기동 완료!"
    exit 0
  fi
  sleep 2
done
echo "헬스체크 타임아웃. 로그를 확인하세요: docker compose logs"
exit 1
```

### Excel 임포트 컨트롤러 패턴
```java
@PostMapping("/api/admin/import/{type}")
@PreAuthorize("hasRole('ADMIN')")
@AuditLog(action = "IMPORT", description = "Excel 데이터 임포트")
public ResponseEntity<ApiResponse<ImportResult>> importExcel(
        @PathVariable String type,
        @RequestParam MultipartFile file) {
    ImportResult result = importService.importExcel(type, file.getInputStream());
    if (result.hasErrors()) {
        // 오류 행이 있으면 오류 리포트 Excel 반환
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    return ResponseEntity.ok(ApiResponse.success(result));
}
```

### application-prod.yml 완성본 패턴
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      connection-timeout: 30000
      idle-timeout: 600000
      keepalive-time: 300000
  jpa:
    show-sql: false
  session:
    jdbc:
      initialize-schema: never
    timeout: 30m

server:
  ssl:
    key-store: ${SSL_KEY_STORE:/app/certs/keystore.p12}
    key-store-password: ${SSL_KEY_STORE_PASSWORD}
    key-store-type: PKCS12
  port: 8443

management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      show-details: never

logging:
  level:
    root: WARN
    com.navy.casualty: INFO
    org.hibernate.SQL: WARN
    org.hibernate.type: WARN
  file:
    name: /app/logs/navy-casualty.log
  logback:
    rollingpolicy:
      max-file-size: 50MB
      max-history: 30
      total-size-cap: 1GB

app:
  version: v1.0.0
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Spring Boot 2.x SSL | Spring Boot 3.x `server.ssl.*` | 2023 | 설정 키 이름 동일, 내부 구현 변경 |
| Playwright 1.x | Playwright 1.59 | 2026 | API 안정화, 속도 개선 |
| OWASP ZAP 2.x | ZAP 현행 | 지속 | Docker 이미지 `ghcr.io/zaproxy/zaproxy` |
| JMeter 5.5 | JMeter 5.6.3 | 2024 | HTTP/2 지원 개선 |

## Open Questions

1. **교육 슬라이드 형식**
   - What we know: Markdown 원본 + pandoc 변환 (D-29)
   - What's unclear: pandoc beamer(LaTeX PDF 슬라이드) vs Marp(HTML 슬라이드) 중 선택
   - Recommendation: Marp가 설치 간편(npm). pandoc beamer는 LaTeX 필요. 에어갭 환경 고려 시 Marp CLI로 HTML 슬라이드 생성 권장.

2. **JMeter 실행 환경**
   - What we know: JMeter CLI 미설치. Docker 이미지 사용 가능.
   - What's unclear: JMX 스크립트 실행을 CI/수동 중 어떤 방식으로 할지
   - Recommendation: JMX 스크립트만 작성하고, 실행 방법은 매뉴얼에 기재. Docker `justb4/jmeter` 또는 로컬 설치 둘 다 안내.

3. **Testcontainers vs H2 for 통합 테스트**
   - What we know: 현재 테스트는 H2 인메모리 사용. 파티셔닝 등 PostgreSQL 전용 기능은 H2에서 불가.
   - What's unclear: 통합 테스트에서 PostgreSQL 파티셔닝 검증이 필요한지
   - Recommendation: 기본 API 통합 테스트는 H2 유지. 파티셔닝 DDL은 Flyway validate + 수동 검증. Testcontainers 도입은 복잡도 대비 이득이 적음.

4. **Excel 임포트 대량 파일 파서**
   - What we know: 내보내기는 SXSSFWorkbook(스트리밍 쓰기). 읽기는 별도 전략 필요.
   - What's unclear: 예상 임포트 데이터 규모
   - Recommendation: POI의 기본 XSSFWorkbook으로 시작 (1만 행 이하 충분). 메모리 이슈 발생 시 streaming reader로 전환.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Docker | 배포 패키징, OWASP ZAP | O | 29.2.1 | -- |
| Node.js | Playwright, 프론트엔드 빌드 | O | 24.13.0 | -- |
| npm | 패키지 설치 | O | 11.6.2 | -- |
| Playwright | E2E 테스트 | O (npx) | 1.59.1 | -- |
| OpenSSL | TLS 인증서 생성 | O | 3.5.5 | -- |
| Java/keytool | PKCS12 변환 대안 | X | -- | openssl pkcs12로 대체 |
| JMeter | 성능 테스트 | X | -- | Docker 이미지 `justb4/jmeter:5.6.3` |
| pandoc | 문서 PDF 변환 | X | -- | 별도 설치 필요 또는 Docker `pandoc/extra` |
| Gradle Wrapper | 백엔드 빌드 | O | gradlew 존재 | -- |

**Missing dependencies with no fallback:**
- 없음 (모든 미설치 도구에 Docker 또는 대안 존재)

**Missing dependencies with fallback:**
- JMeter: Docker 이미지로 실행 가능
- pandoc: Docker 이미지 또는 로컬 설치 안내
- keytool: openssl pkcs12 명령으로 대체

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework (Backend) | JUnit 5 + Spring Boot Test (기존) |
| Framework (Frontend) | Vitest 4.1.2 (기존) + Playwright 1.59.1 (신규) |
| Config file | backend: 없음 (Gradle 기본), frontend: vitest는 vite.config.ts 내장 |
| Quick run (Backend) | `cd backend && ./gradlew test --tests "*IntegrationTest" -x` |
| Quick run (Frontend) | `cd frontend && npx vitest run` |
| E2E run | `cd test/e2e && npx playwright test` |
| Full suite | `cd backend && ./gradlew test && cd ../frontend && npx vitest run && npx playwright test` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| ALL AUTH | 로그인/권한 전체 흐름 | E2E | `npx playwright test login.spec.ts` | Wave 0 |
| ALL DEAD | 사망자 CRUD | E2E + Integration | `npx playwright test dead.spec.ts` | Wave 0 |
| ALL WOND | 상이자 CRUD | E2E + Integration | `npx playwright test wounded.spec.ts` | Wave 0 |
| ALL REVW | 심사+자동반영 | E2E + Integration | `npx playwright test review.spec.ts` | Wave 0 |
| ALL DOCU | 문서 출력 | E2E | `npx playwright test document.spec.ts` | Wave 0 |
| ALL STAT | 통계 조회 | E2E | `npx playwright test statistics.spec.ts` | Wave 0 |
| STAT-07 | 응답시간 5초 | Performance | JMeter JMX 실행 (수동) | Wave 0 |
| INFRA-05 | HTTPS | Integration | Spring Boot 통합 테스트 | Wave 0 |
| D-26 | data-check API | Unit | `./gradlew test --tests "*DataCheckController*"` | Wave 0 |
| D-27 | 비밀번호 변경 강제 | Unit + E2E | Unit: `*PasswordChangeFilter*`, E2E: login.spec.ts | Wave 0 |
| D-34 | Excel 임포트 검증 | Unit | `./gradlew test --tests "*ImportService*"` | Wave 0 |

### Sampling Rate
- **Per task commit:** `cd backend && ./gradlew test`
- **Per wave merge:** `cd backend && ./gradlew test && cd ../frontend && npx vitest run`
- **Phase gate:** Full suite + Playwright E2E + JMeter 보고서 확인

### Wave 0 Gaps
- [ ] `test/e2e/playwright.config.ts` -- Playwright 설정 파일
- [ ] `test/e2e/specs/*.spec.ts` -- 7개 핵심 시나리오 E2E 테스트
- [ ] `@playwright/test` devDependency 설치 (frontend 또는 root)
- [ ] `test/performance/load-test.jmx` -- JMeter 시나리오
- [ ] 통합 테스트 클래스 (현재 단위 테스트만 존재)

## Sources

### Primary (HIGH confidence)
- 프로젝트 소스 코드 직접 분석 (docker-compose.yml, Dockerfile, build.gradle.kts, package.json, SecurityConfig.java, application.yml/prod.yml, 기존 테스트 25개)
- npm registry 버전 확인 (@playwright/test 1.59.1, rollup-plugin-visualizer 7.0.1)
- Docker 29.2.1, Node 24.13.0, OpenSSL 3.5.5 환경 확인

### Secondary (MEDIUM confidence)
- Spring Boot 3.5 server.ssl.* 설정 패턴 (Spring Boot 공식 문서 기반)
- PostgreSQL RANGE 파티셔닝 DDL 패턴 (PostgreSQL 16 공식 문서 기반)
- OWASP ZAP Docker 이미지 사용 패턴 (zaproxy 공식 GitHub 기반)

### Tertiary (LOW confidence)
- JasperReports로 월별 감사 보고서 자동 생성 패턴 -- 기존 DocumentGenerationService 패턴 재활용 가능하지만, 스케줄링(@Scheduled) + 집계 쿼리 조합은 구현 시 검증 필요

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - 기존 프로젝트 스택 100% 재활용, 추가 도구 버전 검증 완료
- Architecture: HIGH - 기존 패턴(Flyway, JasperReports, POI, Docker Compose) 확장
- Pitfalls: MEDIUM - 파티셔닝/Docker save 등 실제 환경에서만 확인 가능한 이슈 존재
- 배포 스크립트: HIGH - 표준 Docker/pg_dump 패턴
- 운영 문서: MEDIUM - pandoc 환경 설치 필요, 교육 슬라이드 형식 미확정

**Research date:** 2026-04-04
**Valid until:** 2026-05-04 (안정 스택, 30일)
