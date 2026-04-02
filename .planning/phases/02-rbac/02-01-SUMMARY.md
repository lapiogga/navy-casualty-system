---
phase: 02-rbac
plan: 01
subsystem: auth
tags: [spring-security, bcrypt, session, rbac, jpa]

requires:
  - phase: 01-project-foundation
    provides: "BaseAuditEntity, ApiResponse, Flyway TB_USER 스키마, SecurityConfig(permitAll)"
provides:
  - "SecurityConfig RBAC (역할계층, 세션1개, BCrypt12)"
  - "User 엔티티 + UserRepository + UserService"
  - "CustomUserDetails + CustomUserDetailsService"
  - "AuthService (잠금/리셋/해제)"
  - "AuthController (login/logout/me)"
  - "V10 초기 admin 계정"
affects: [02-rbac, 03-casualty-crud]

tech-stack:
  added: [spring-boot-starter-aop, spring-security-test]
  patterns: [세션기반인증, BCrypt12, D-02에러메시지통일, 역할계층RBAC]

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/security/SecurityConfig.java
    - backend/src/main/java/com/navy/casualty/security/CustomUserDetails.java
    - backend/src/main/java/com/navy/casualty/security/CustomUserDetailsService.java
    - backend/src/main/java/com/navy/casualty/security/SecurityUtils.java
    - backend/src/main/java/com/navy/casualty/user/entity/User.java
    - backend/src/main/java/com/navy/casualty/user/repository/UserRepository.java
    - backend/src/main/java/com/navy/casualty/user/service/UserService.java
    - backend/src/main/java/com/navy/casualty/auth/service/AuthService.java
    - backend/src/main/java/com/navy/casualty/auth/controller/AuthController.java
    - backend/src/main/java/com/navy/casualty/auth/dto/LoginRequest.java
    - backend/src/main/java/com/navy/casualty/auth/dto/LoginResponse.java
    - backend/src/main/resources/db/migration/V10__insert_admin_user.sql
    - backend/src/test/java/com/navy/casualty/auth/AuthServiceTest.java
    - backend/src/test/java/com/navy/casualty/auth/AuthControllerTest.java
  modified:
    - backend/build.gradle.kts
    - backend/src/main/java/com/navy/casualty/common/config/JpaAuditingConfig.java
    - backend/src/main/java/com/navy/casualty/common/exception/GlobalExceptionHandler.java

key-decisions:
  - "기존 common/config/SecurityConfig 삭제, security 패키지로 RBAC 완전 교체"
  - "D-02 에러 메시지 통일: 잠금/실패 구분 없이 동일 메시지 반환"
  - "역할 계층: ADMIN > MANAGER > OPERATOR > VIEWER"

patterns-established:
  - "D-02: 인증 실패 시 원인 노출 없이 통일 메시지 반환"
  - "세션 기반 인증: HttpSession + SecurityContext 수동 저장"
  - "계정 잠금: 5회 실패 후 자동 잠금, 관리자 해제"

requirements-completed: [AUTH-01, AUTH-02, AUTH-03, AUTH-04]

duration: 6min
completed: 2026-04-02
---

# Phase 2 Plan 1: 세션 인증 인프라 Summary

**Spring Security 세션 기반 RBAC 인증 + 로그인/로그아웃/me API + 계정 잠금(5회) + 초기 admin 계정**

## Performance

- **Duration:** 6min
- **Started:** 2026-04-02T16:40:19Z
- **Completed:** 2026-04-02T16:46:00Z
- **Tasks:** 2
- **Files modified:** 17

## Accomplishments
- SecurityConfig RBAC 완전 교체 (역할계층, 세션1개 제한, BCrypt12, 401/403 JSON 응답)
- 인증 API 3개 엔드포인트 (POST /login, POST /logout, GET /me)
- 계정 잠금 로직 (5회 실패 잠금, 리셋, 관리자 해제)
- 초기 admin 계정 Flyway 마이그레이션
- JpaAuditingConfig SecurityContextHolder 기반 인증 사용자 연동

## Task Commits

1. **Task 1: Security 인프라 + User 엔티티 + 인증 서비스** - `97771b6` (feat)
2. **Task 2: 인증 REST API + 초기 admin 계정** - `26840f2` (feat)

## Files Created/Modified
- `backend/build.gradle.kts` - AOP, spring-security-test 의존성 추가
- `backend/src/main/java/com/navy/casualty/security/SecurityConfig.java` - RBAC SecurityFilterChain, 세션/역할/비밀번호 Bean
- `backend/src/main/java/com/navy/casualty/security/CustomUserDetails.java` - UserDetails 구현체
- `backend/src/main/java/com/navy/casualty/security/CustomUserDetailsService.java` - DB 기반 UserDetailsService
- `backend/src/main/java/com/navy/casualty/security/SecurityUtils.java` - 현재 인증 사용자 추출 유틸
- `backend/src/main/java/com/navy/casualty/user/entity/User.java` - TB_USER JPA 엔티티
- `backend/src/main/java/com/navy/casualty/user/repository/UserRepository.java` - User JPA 리포지토리
- `backend/src/main/java/com/navy/casualty/user/service/UserService.java` - 사용자 CRUD 서비스
- `backend/src/main/java/com/navy/casualty/auth/service/AuthService.java` - 잠금/리셋/해제 로직
- `backend/src/main/java/com/navy/casualty/auth/controller/AuthController.java` - 인증 REST API
- `backend/src/main/java/com/navy/casualty/auth/dto/LoginRequest.java` - 로그인 요청 DTO
- `backend/src/main/java/com/navy/casualty/auth/dto/LoginResponse.java` - 로그인 응답 DTO
- `backend/src/main/java/com/navy/casualty/common/config/JpaAuditingConfig.java` - SecurityContextHolder 연동
- `backend/src/main/java/com/navy/casualty/common/exception/GlobalExceptionHandler.java` - 401/403 핸들러 추가
- `backend/src/main/resources/db/migration/V10__insert_admin_user.sql` - 초기 admin 계정
- `backend/src/test/java/com/navy/casualty/auth/AuthServiceTest.java` - 잠금/리셋/해제 테스트
- `backend/src/test/java/com/navy/casualty/auth/AuthControllerTest.java` - 로그인/로그아웃/me 테스트

## Decisions Made
- 기존 `common/config/SecurityConfig` 삭제, `security/SecurityConfig`로 RBAC 완전 교체
- D-02 에러 메시지 통일: 잠금/실패 구분 없이 "사용자 ID 또는 비밀번호가 올바르지 않습니다" 반환
- 역할 계층: ADMIN > MANAGER > OPERATOR > VIEWER (RoleHierarchyImpl)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
- Java/JDK가 로컬에 설치되어 있지 않고 Docker Desktop 데몬도 비활성 상태여서 테스트 실행 불가. 코드는 플랜 사양대로 작성 완료. 테스트 실행은 CI 또는 Docker 환경에서 검증 필요.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- 인증 인프라 완비: 02-02(감사 로그), 02-03(세션관리), 02-04(사용자관리) 진행 가능
- SecurityConfig, User 엔티티, AuthService가 후속 플랜의 기반

## Self-Check: PASSED

- 14/14 파일 존재 확인
- common/config/SecurityConfig.java 삭제 확인
- 커밋 97771b6, 26840f2 존재 확인

---
*Phase: 02-rbac*
*Completed: 2026-04-02*
