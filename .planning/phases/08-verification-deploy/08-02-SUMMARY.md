---
phase: 08-verification-deploy
plan: 02
subsystem: infra
tags: [spring-security, tls, hikari, docker, healthcheck, actuator]

requires:
  - phase: 01-foundation
    provides: Docker Compose 기본 구성, application.yml
  - phase: 02-auth-rbac
    provides: SecurityConfig RBAC 설정
provides:
  - 보안 헤더 (HSTS, X-Content-Type-Options, X-Frame-Options)
  - TLS HTTP->HTTPS 리다이렉트 (prod + ssl.enabled=true)
  - 운영 프로파일 HikariCP/로그/세션 설정
  - Docker healthcheck + restart 정책
  - StartupValidator 부팅 검증
affects: []

tech-stack:
  added: []
  patterns:
    - "@Profile(prod) + @ConditionalOnProperty 조건부 Bean 활성화"
    - "ApplicationRunner 부팅 시 환경/데이터 검증"

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/common/config/TlsConfig.java
    - backend/src/main/java/com/navy/casualty/common/init/StartupValidator.java
  modified:
    - backend/src/main/java/com/navy/casualty/security/SecurityConfig.java
    - backend/src/main/resources/application-prod.yml
    - backend/src/main/resources/application.yml
    - docker-compose.prod.yml

key-decisions:
  - "TLS 기본 비활성화 (ssl.enabled=false) - 환경변수로 활성화 전환"
  - "StartupValidator 실패 시 경고 로그만 출력, 기동 차단 안 함"
  - "Actuator health만 노출, show-details: never"

patterns-established:
  - "조건부 TLS: @ConditionalOnProperty(server.ssl.enabled=true)로 개발/운영 환경 자동 분기"
  - "부팅 검증: ApplicationRunner로 환경변수, 코드 테이블, 관리자 계정 점검"

requirements-completed: [INFRA-01, INFRA-03, INFRA-05, AUTH-03, AUTH-04]

duration: 3min
completed: 2026-04-04
---

# Phase 8 Plan 02: 보안 강화 + 운영 설정 Summary

**Spring Security 보안 헤더 3종, TLS 리다이렉트, HikariCP/로그/세션 운영 프로파일, Docker healthcheck, StartupValidator 부팅 검증 완성**

## Performance

- **Duration:** 3 min
- **Started:** 2026-04-04T08:58:16Z
- **Completed:** 2026-04-04T09:01:15Z
- **Tasks:** 2
- **Files modified:** 6

## Accomplishments
- SecurityConfig에 HSTS, X-Content-Type-Options, X-Frame-Options 보안 헤더 추가
- TlsConfig: prod 프로파일에서 SSL 활성화 시 HTTP->HTTPS 리다이렉트
- application-prod.yml: HikariCP 풀 10개, 로그 롤링 50MB/30일/1GB, 세션 30분 + 보안 쿠키
- Actuator health만 외부 노출 (show-details: never)
- Docker healthcheck (app: curl actuator, db: pg_isready) + unless-stopped 재시작 정책
- StartupValidator: 부팅 시 DB_PASSWORD, PII_ENCRYPTION_KEY 환경변수 + 코드 테이블 + admin 계정 검증

## Task Commits

1. **Task 1: 보안 헤더 + TLS 설정 + Actuator 제한 + 운영 프로파일** - `596062b` (feat)
2. **Task 2: Docker healthcheck + restart 정책 + StartupValidator** - `cfbbef1` (feat)

## Files Created/Modified
- `backend/src/main/java/com/navy/casualty/security/SecurityConfig.java` - HSTS, X-Content-Type-Options, X-Frame-Options 헤더 추가
- `backend/src/main/java/com/navy/casualty/common/config/TlsConfig.java` - HTTP->HTTPS 리다이렉트 (prod + SSL)
- `backend/src/main/resources/application-prod.yml` - HikariCP, 로그 롤링, 세션 보안 쿠키, Actuator 제한
- `backend/src/main/resources/application.yml` - Actuator health 노출 설정
- `docker-compose.prod.yml` - healthcheck + unless-stopped 재시작 정책
- `backend/src/main/java/com/navy/casualty/common/init/StartupValidator.java` - 부팅 시 환경/데이터 검증

## Decisions Made
- TLS 기본 비활성화 (ssl.enabled=false) - 개발 환경에서는 HTTP만, 운영 환경에서 환경변수로 활성화
- StartupValidator 실패 시 경고 로그만 출력, 기동을 차단하지 않음 (D-28 명시)
- Actuator는 health 엔드포인트만 외부 노출, show-details: never

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- JAVA_HOME 미설정 환경이었으나 Eclipse Adoptium JDK 21 경로를 직접 지정하여 컴파일 검증 성공

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- 보안 헤더, TLS, 운영 프로파일, Docker healthcheck, 부팅 검증 모두 완성
- TLS 실제 활성화는 SSL 인증서 + SSL_KEYSTORE_PASSWORD 환경변수 설정 필요

## Self-Check: PASSED

All 7 files verified present. Both task commits (596062b, cfbbef1) confirmed in git log.

---
*Phase: 08-verification-deploy*
*Completed: 2026-04-04*
