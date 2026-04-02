---
phase: 02-rbac
plan: 03
subsystem: frontend-auth
tags: [login, auth, rbac, admin, audit-log, react]
dependency_graph:
  requires: [02-01]
  provides: [frontend-auth-flow, admin-ui, route-guard]
  affects: [frontend-routes, frontend-layout]
tech_stack:
  added: [react-context]
  patterns: [auth-context, route-guard, role-based-menu]
key_files:
  created:
    - frontend/src/types/auth.ts
    - frontend/src/hooks/useAuth.tsx
    - frontend/src/utils/rrnMask.ts
    - frontend/src/pages/LoginPage.tsx
    - frontend/src/components/auth/AuthGuard.tsx
    - frontend/src/pages/admin/AdminPage.tsx
    - frontend/src/pages/admin/UserManagement.tsx
    - frontend/src/pages/admin/AuditLogViewer.tsx
    - frontend/src/__tests__/LoginPage.test.tsx
    - frontend/src/__tests__/AdminPage.test.tsx
  modified:
    - frontend/src/routes/index.tsx
    - frontend/src/components/layout/AppLayout.tsx
    - frontend/src/api/client.ts
    - frontend/src/main.tsx
    - frontend/src/test/setup.ts
    - frontend/src/__tests__/AppLayout.test.tsx
decisions:
  - AuthProvider를 main.tsx에서 RouterProvider 감싸기 (전역 인증 컨텍스트)
  - useAuth.ts -> useAuth.tsx 확장자 (JSX 포함으로 vite/oxc 호환)
  - AuthGuard/AdminGuard 별도 컴포넌트로 분리 (재사용성)
metrics:
  duration: 10min
  completed: 2026-04-02
---

# Phase 02 Plan 03: 프론트엔드 인증 + 관리자 화면 Summary

React Context 기반 인증 훅(useAuth) + 좌/우 분할 로그인 페이지 + 라우트 가드 + 관리자 화면(사용자 CRUD, 감사 로그 조회) 구현

## Task Execution

### Task 1: 인증 타입 + useAuth 훅 + 로그인 페이지 + 라우트 가드
- **Commit:** af59c03
- **Files:** auth.ts, useAuth.tsx, rrnMask.ts, LoginPage.tsx, AuthGuard.tsx, AppLayout.tsx, routes/index.tsx, client.ts, main.tsx
- **Result:** D-01/D-02/D-03 사양 충족. 좌/우 분할 레이아웃 로그인, 모호한 에러 메시지, 라우트 가드, AppLayout 사용자 정보 + 역할 기반 메뉴 필터링

### Task 2: 관리자 화면 -- 사용자 관리 + 감사 로그 조회
- **Commit:** ee8fa8b
- **Files:** AdminPage.tsx, UserManagement.tsx, AuditLogViewer.tsx, AdminPage.test.tsx
- **Result:** D-06/D-07 사양 충족. Tabs(사용자 관리, 감사 로그), 사용자 CRUD Table+Modal, 감사 로그 필터+읽기 전용 Table

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] useAuth.ts -> useAuth.tsx 확장자 변경**
- **Found during:** Task 1
- **Issue:** vite/oxc가 .ts 파일의 JSX 구문을 파싱하지 못함
- **Fix:** hooks/useAuth.ts를 hooks/useAuth.tsx로 확장자 변경
- **Files modified:** frontend/src/hooks/useAuth.tsx

**2. [Rule 3 - Blocking] test setup에 matchMedia + ResizeObserver mock 추가**
- **Found during:** Task 1, Task 2
- **Issue:** antd 컴포넌트가 jsdom에 없는 window.matchMedia, ResizeObserver 사용
- **Fix:** test/setup.ts에 두 mock 추가
- **Files modified:** frontend/src/test/setup.ts

**3. [Rule 1 - Bug] 기존 AppLayout.test.tsx 업데이트**
- **Found during:** Task 1
- **Issue:** AppLayout이 useAuth 의존성을 가지게 되어 기존 테스트 실패
- **Fix:** useAuth mock 추가 + 사용자 정보/로그아웃 버튼 테스트 추가
- **Files modified:** frontend/src/__tests__/AppLayout.test.tsx

## Decisions Made

1. **AuthProvider 위치:** main.tsx에서 RouterProvider를 감싸는 방식으로 전역 인증 컨텍스트 제공
2. **가드 분리:** AuthGuard(인증), AdminGuard(ADMIN 역할) 별도 컴포넌트로 분리하여 라우트별 적용
3. **확장자:** useAuth는 JSX를 반환하므로 .tsx 사용 (plan의 .ts 대신)

## Known Stubs

없음 - 모든 컴포넌트가 API 클라이언트에 연결되어 있고, 백엔드 API(02-01에서 구현된 auth, 02-04에서 구현될 admin)와 경로가 일치함.

## Test Results

- 전체 테스트: 17 passed (4 test files)
- LoginPage: 5 tests (렌더링, 입력 필드, 버튼)
- AdminPage: 4 tests (Tabs, 탭 이름, 기본 선택)
- AppLayout: 4 tests (타이틀, 메뉴, Sider, 사용자 정보)
- Routes: 4 tests (라우트 설정)

## Self-Check: PASSED

- 11/11 파일 존재 확인
- 2/2 커밋 확인 (af59c03, ee8fa8b)
