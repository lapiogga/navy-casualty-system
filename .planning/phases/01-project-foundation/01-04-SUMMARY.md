---
phase: 01-project-foundation
plan: 04
type: gap_closure
status: complete
started: 2026-04-03
completed: 2026-04-03
---

## Summary

Phase 1 UAT에서 진단된 2건의 gap을 수정했다.

## Accomplishments

1. **V12 Flyway 마이그레이션으로 admin BCrypt 해시 교정** - V10에 잘못 하드코딩된 BCrypt 해시를 V12 fixup SQL로 올바른 값($2a$12$, strength=12)으로 UPDATE 처리
2. **401 리다이렉트를 router.navigate로 전환** - client.ts의 `window.location.href`(full page reload)를 `router.navigate('/login')`(SPA 네비게이션)으로 변경하여 React Router 상태 유지
3. **App.tsx dead code 삭제** - main.tsx에서 import되지 않는 불필요한 파일 제거
4. **AuditLogViewer 미사용 import 정리** - Space import 제거로 빌드 에러 해소

## Key Files

### Created
- `backend/src/main/resources/db/migration/V12__fix_admin_bcrypt_hash.sql`

### Modified
- `frontend/src/api/client.ts` - window.location.href -> router.navigate
- `frontend/src/pages/admin/AuditLogViewer.tsx` - unused import 제거

### Deleted
- `frontend/src/App.tsx` - dead code

## Gaps Resolved

| Gap | Severity | Resolution |
|-----|----------|------------|
| V10 admin BCrypt 해시 불일치 | blocker | V12 fixup SQL |
| 401 리다이렉트 미작동 | major | router.navigate 전환 |

## Self-Check: PASSED
- V12 SQL 파일 존재 및 BCrypt12 해시 포함 확인
- App.tsx 삭제 확인
- client.ts에 router.navigate 존재, window.location.href 제거 확인
- 프론트엔드 빌드 성공 확인
