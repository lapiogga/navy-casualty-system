---
phase: 08-verification-deploy
plan: 03
subsystem: auth-data-init
tags: [flyway, password-change, data-check, security]
dependency_graph:
  requires: [01-01, 02-01]
  provides: [initial-data, password-change-flow, data-check-api]
  affects: [auth, admin, frontend-routing]
tech_stack:
  added: []
  patterns: [password-change-guard, data-integrity-check]
key_files:
  created:
    - backend/src/main/resources/db/migration/V18__replace_mock_with_real_code_data.sql
    - backend/src/main/resources/db/migration/V19__add_password_changed_column.sql
    - backend/src/main/java/com/navy/casualty/auth/dto/ChangePasswordRequest.java
    - backend/src/main/java/com/navy/casualty/admin/controller/AdminController.java
    - backend/src/main/java/com/navy/casualty/admin/dto/DataCheckResponse.java
    - frontend/src/pages/ChangePasswordPage.tsx
  modified:
    - backend/src/main/java/com/navy/casualty/user/entity/User.java
    - backend/src/main/java/com/navy/casualty/security/CustomUserDetails.java
    - backend/src/main/java/com/navy/casualty/auth/dto/LoginResponse.java
    - backend/src/main/java/com/navy/casualty/auth/service/AuthService.java
    - backend/src/main/java/com/navy/casualty/auth/controller/AuthController.java
    - frontend/src/types/auth.ts
    - frontend/src/hooks/useAuth.tsx
    - frontend/src/routes/index.tsx
    - frontend/src/pages/LoginPage.tsx
    - frontend/src/components/auth/AuthGuard.tsx
decisions:
  - 패키지 구조 차이 대응: 플랜의 security 패키지 대신 실제 auth/user 패키지 구조에 맞춰 구현
  - PasswordGuard 컴포넌트 분리: AuthGuard 내부에서 비밀번호 변경 강제 리다이렉트를 별도 가드로 분리
  - refreshUser 메서드 추가: 비밀번호 변경 후 사용자 정보 갱신을 위해 useAuth에 추가
metrics:
  duration: 11min
  completed: "2026-04-04T08:53:30Z"
  tasks_completed: 2
  tasks_total: 2
  files_changed: 16
---

# Phase 08 Plan 03: 초기 데이터 적재 + 비밀번호 변경 강제 Summary

V18 실 코드 데이터 적재(계급/군구분/사망유형/부대/보훈청) + V19 password_changed 컬럼 + 비밀번호 변경 API/UI + 데이터 검증 API

## Changes Made

### Task 1: Flyway V18 실 코드 데이터 + V19 password_changed + 백엔드 API
- V18: Mock 데이터를 해군 실 코드 데이터로 교체 (계급 19건, 군구분 7건, 사망유형 8건, 부대 22건, 보훈청 15건)
- V19: TB_USER에 password_changed BOOLEAN 컬럼 추가 (기본값 false)
- User 엔티티에 passwordChanged 필드 + changePassword() 메서드
- CustomUserDetails, LoginResponse에 passwordChanged 필드 전파
- AuthController에 PUT /api/auth/change-password 엔드포인트 (현재 비밀번호 검증 + 새 비밀번호 8자 이상 영문+숫자)
- AdminController에 GET /api/admin/data-check 엔드포인트 (코드 테이블 건수 + admin 존재 여부 + appVersion)
- Commit: 1c6acd2

### Task 2: 프론트엔드 비밀번호 변경 화면 + 리다이렉트
- ChangePasswordPage: 현재/새/확인 비밀번호 Ant Design Form (첫 로그인 시 안내 메시지)
- PasswordGuard 컴포넌트: passwordChanged=false면 /change-password로 리다이렉트
- LoginPage: 로그인 성공 후 passwordChanged 분기 (useEffect)
- useAuth에 refreshUser 메서드 추가
- AuthUser 타입에 passwordChanged 필드 추가
- routes에 /change-password 라우트 추가 (AuthGuard 내부, AppLayout 외부)
- Commit: 51ed273

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 패키지 구조 불일치 해결**
- **Found during:** Task 1
- **Issue:** 플랜은 security/entity, security/service, security/controller 패키지를 가정했으나 실제는 user/entity, auth/service, auth/controller 패키지 구조
- **Fix:** 실제 패키지 구조에 맞춰 파일 생성/수정 (admin 패키지는 신규 생성)
- **Files modified:** 모든 백엔드 파일

**2. [Rule 2 - Missing] LoginPage navigate 패턴 수정**
- **Found during:** Task 2
- **Issue:** 렌더 본문에서 직접 navigate 호출 시 React 경고 발생 가능
- **Fix:** useEffect 패턴으로 전환하여 사이드이펙트 분리
- **Files modified:** frontend/src/pages/LoginPage.tsx

## Decisions Made

1. admin 패키지 신규 생성 (AdminController, DataCheckResponse) - 기존 user 패키지와 관심사 분리
2. PasswordGuard를 AuthGuard와 별도 컴포넌트로 분리 - 관심사 명확화
3. refreshUser 메서드를 useAuth에 추가 - 비밀번호 변경 후 세션 갱신

## Verification Results

- V18, V19 마이그레이션 파일 존재: PASS
- 백엔드 compileJava 성공: PASS
- 프론트엔드 tsc --noEmit 성공: PASS
- /api/auth/change-password API 존재: PASS
- /api/admin/data-check API 존재: PASS
- ChangePasswordPage 화면 존재: PASS

## Self-Check: PASSED
