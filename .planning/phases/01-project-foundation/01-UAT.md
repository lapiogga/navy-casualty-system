---
status: diagnosed
phase: 01-project-foundation
source: [01-01-SUMMARY.md, 01-02-SUMMARY.md, 01-03-SUMMARY.md]
started: 2026-04-02T23:17:52Z
updated: 2026-04-03T00:00:00Z
---

## Current Test

[testing complete]

## Tests

### 1. Cold Start Smoke Test
expected: `docker-compose up --build`로 앱과 DB를 기동하면 Spring Boot가 에러 없이 부팅되고, Flyway 마이그레이션 V1~V9가 완료되며, 헬스체크 또는 기본 API 호출이 응답을 반환한다.
result: pass

### 2. Gradle 백엔드 빌드
expected: `cd backend && ./gradlew build`를 실행하면 컴파일, 테스트 모두 성공하고 BUILD SUCCESSFUL 메시지가 출력된다.
result: pass

### 3. 프론트엔드 빌드
expected: `cd frontend && npm run build`를 실행하면 에러 없이 빌드가 완료되고 dist/ 폴더에 번들 파일이 생성된다.
result: pass

### 4. 프론트엔드 개발 서버 + AppLayout 표시
expected: `cd frontend && npm run dev`로 개발 서버를 시작하고 브라우저에서 접속하면, 왼쪽에 Sider(220px, 어두운 테마), 상단에 Header(64px), 가운데 Content 영역이 보인다.
result: pass

### 5. 메뉴 네비게이션 (6개 항목)
expected: Sider의 메뉴에 사망자 관리, 상이자 관리, 전공사상심사, 문서 출력, 통계/현황, 시스템 관리 6개 항목이 있고, 클릭하면 각 라우트로 이동하며 "이 기능은 아직 개발 중입니다" placeholder가 표시된다.
result: pass

### 6. API 클라이언트 401 리다이렉트
expected: 인증 없이 보호된 API를 호출하면 401 응답 시 /login 경로로 리다이렉트된다 (Phase 2 인증 적용 후 확인 가능).
result: issue
reported: "redirect되지 않음"
severity: major

### 7. Docker Compose 개발 환경
expected: `docker-compose up`으로 app + db 서비스가 시작되고, PostgreSQL healthcheck 통과 후 app이 기동된다. localhost:8080으로 접근 가능하다.
result: pass

### 8. 환경변수 템플릿
expected: `.env.example` 파일에 DB_PASSWORD, SPRING_PROFILES_ACTIVE, PII_ENCRYPTION_KEY 등 필수 환경변수가 문서화되어 있고, `.env`는 .gitignore에 포함되어 있다.
result: pass

### 9. V10 초기 admin BCrypt 해시 오류
expected: V10 마이그레이션의 admin 계정 BCrypt 해시($2a$12$LJ3m...)가 비밀번호 'admin1234'와 일치해야 한다.
result: issue
reported: "V10 마이그레이션의 BCrypt 해시가 admin1234와 불일치. pgcrypto crypt()로 DB 직접 업데이트 후 로그인 성공."
severity: blocker

## Summary

total: 9
passed: 7
issues: 2
pending: 0
skipped: 0
blocked: 0

## Gaps

- truth: "V10 초기 admin 계정 BCrypt 해시가 비밀번호 admin1234와 일치한다"
  status: failed
  reason: "User reported: V10 마이그레이션의 BCrypt 해시가 admin1234와 불일치. pgcrypto crypt()로 DB 직접 업데이트 후 로그인 성공."
  severity: blocker
  test: 9
  root_cause: "V10__insert_admin_user.sql의 사전 해싱된 BCrypt 값이 admin1234에 대해 생성된 것이 아님"
  artifacts:
    - path: "backend/src/main/resources/db/migration/V10__insert_admin_user.sql"
      issue: "BCrypt 해시가 admin1234와 불일치"
  missing:
    - "V10 SQL의 BCrypt 해시를 admin1234에 대해 올바르게 재생성"
  debug_session: ""

- truth: "인증 없이 보호된 API 호출 시 401 응답에서 /login으로 리다이렉트된다"
  status: failed
  reason: "User reported: redirect되지 않음"
  severity: major
  test: 6
  root_cause: "main.tsx와 App.tsx에 RouterProvider가 이중으로 존재하며, 401 인터셉터가 window.location.href 직접 변경 방식이라 React Router 상태가 갱신되지 않음"
  artifacts:
    - path: "frontend/src/App.tsx"
      issue: "중복 RouterProvider - main.tsx에 이미 AuthProvider > RouterProvider 구성됨"
    - path: "frontend/src/api/client.ts"
      issue: "401 인터셉터가 window.location.href 사용 - React Router navigate 대신"
    - path: "frontend/src/main.tsx"
      issue: "AuthProvider > RouterProvider 정상 구조이나 App.tsx와 충돌"
  missing:
    - "App.tsx의 중복 RouterProvider 제거 또는 main.tsx와 통합"
    - "401 인터셉터에서 React Router navigate 패턴으로 전환 검토"
