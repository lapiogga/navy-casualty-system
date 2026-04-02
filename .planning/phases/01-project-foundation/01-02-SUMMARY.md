---
phase: 01-project-foundation
plan: 02
subsystem: frontend
tags: [react, vite, antd, typescript, routing]
dependency_graph:
  requires: []
  provides: [frontend-shell, frontend-routing, api-client]
  affects: [01-03]
tech_stack:
  added: [react-18, typescript-5.9, vite-8, antd-6, tanstack-query-5, axios, react-router-dom-7, vitest-4]
  patterns: [configprovider-theme, browser-router, layout-sider-header-content]
key_files:
  created:
    - frontend/src/main.tsx
    - frontend/src/components/layout/AppLayout.tsx
    - frontend/src/components/layout/PlaceholderPage.tsx
    - frontend/src/pages/ErrorPage.tsx
    - frontend/src/pages/NotFoundPage.tsx
    - frontend/src/routes/index.tsx
    - frontend/src/api/client.ts
    - frontend/vite.config.ts
    - frontend/vitest.config.ts
  modified:
    - frontend/package.json
decisions:
  - Ant Design 6.x 채택 (플랜의 5.x 대신 최신 안정 버전 사용, API 호환)
  - react-router-dom v7 사용 (createBrowserRouter API 호환 유지)
  - @types/react v18로 고정 (React 18 호환)
metrics:
  duration: 333s
  completed: "2026-04-03T00:27:00Z"
  tasks_completed: 2
  tasks_total: 2
  tests_passed: 7
  tests_total: 7
---

# Phase 01 Plan 02: React 프론트엔드 뼈대 + Ant Design Layout + 라우팅 Summary

React 18 + TypeScript + Ant Design 6.x 프론트엔드를 Vite 8로 구성하고, Sider(220px) + Header(64px) + Content Layout 셸과 6개 메뉴 라우팅을 완성했다.

## Task Results

| Task | Name | Commit | Status |
|------|------|--------|--------|
| 1 | Vite + React 프로젝트 초기화 + ConfigProvider + Axios | d7ec63b | DONE |
| 2 | AppLayout + 라우팅 + PlaceholderPage + 테스트 | c7fec4e | DONE |

## Key Artifacts

- **AppLayout.tsx**: Ant Design Layout 셸 (Sider 220px dark collapsible + Header 64px + Content). 6개 메뉴 항목, useNavigate/useLocation 기반 라우팅 연동
- **routes/index.tsx**: createBrowserRouter로 6개 라우트 + /login + NotFound. 루트는 /dead로 리다이렉트
- **main.tsx**: ConfigProvider(koKR 로케일, 테마 토큰) + QueryClientProvider + RouterProvider 래핑
- **api/client.ts**: Axios 인스턴스 (/api baseURL, 30s timeout, 401 인터셉터)

## Decisions Made

1. **Ant Design 6.x 사용**: 플랜에서 5.x를 지정했으나, npm 최신 안정 버전이 6.x. ConfigProvider/Layout/Menu API 호환.
2. **react-router-dom v7**: 플랜 예시의 createBrowserRouter API가 v7에서도 동일하게 동작.
3. **@types/react v18 고정**: React 18과 타입 호환성 보장을 위해 v19 대신 v18 사용.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] create-vite CLI 인터랙티브 모드 실패**
- **Found during:** Task 1
- **Issue:** `npm create vite@latest` 명령이 인터랙티브 프롬프트로 인해 취소됨
- **Fix:** npm init -y로 수동 초기화 후 의존성 직접 설치
- **Files modified:** frontend/package.json
- **Commit:** d7ec63b

**2. [Rule 2 - Missing] Ant Design 6.x / react-router-dom v7 버전 차이**
- **Found during:** Task 1
- **Issue:** 플랜 기준 antd 5.x, react-router-dom v6 대신 최신 버전 설치됨
- **Fix:** API 호환성 확인 후 그대로 진행. @types/react만 v18로 다운그레이드
- **Commit:** d7ec63b

## Verification Results

- `npm run build`: BUILD SUCCESS (2.58s)
- `npm run test:run`: 7 tests passed (2 files)
- AppLayout: Sider 220px dark, Header 64px white, Content margin 16 padding 24
- 6개 메뉴: 사망자 관리, 상이자 관리, 전공사상심사, 문서 출력, 통계/현황, 시스템 관리

## Known Stubs

- **PlaceholderPage**: 모든 6개 메뉴 페이지가 "이 기능은 아직 개발 중입니다" placeholder. Phase 3~7에서 실제 CRUD 화면으로 교체 예정.
- **로그인 페이지**: /login 라우트가 PlaceholderPage. Phase 2에서 구현 예정.
- **Header 우측 영역**: Phase 2에서 사용자 정보/로그아웃 버튼 추가 예정.
- **401 인터셉터**: window.location.href 리다이렉트. Phase 2에서 React Router 기반으로 개선 예정.

이 스텁들은 플랜 목적(Layout 셸 + 라우팅 뼈대)에 부합하며, 후속 Phase에서 해소된다.

## Self-Check: PASSED

- 9/9 key files exist
- 2/2 commits found (d7ec63b, c7fec4e)
