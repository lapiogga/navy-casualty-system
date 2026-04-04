---
phase: 8
slug: verification-deploy
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-04-04
---

# Phase 8 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 (backend) / Playwright (E2E) / JMeter (performance) |
| **Config file** | `backend/build.gradle.kts` (JUnit), `frontend/playwright.config.ts` (Wave 0 installs) |
| **Quick run command** | `cd backend && ./gradlew test --no-daemon` |
| **Full suite command** | `cd backend && ./gradlew test --no-daemon && cd ../frontend && npx playwright test` |
| **Estimated runtime** | ~120 seconds |

---

## Sampling Rate

- **After every task commit:** Run `cd backend && ./gradlew test --no-daemon`
- **After every plan wave:** Run full suite command
- **Before `/gsd:verify-work`:** Full suite must be green
- **Max feedback latency:** 120 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|-----------|-------------------|-------------|--------|
| 08-01-01 | 01 | 1 | INFRA-01~05 | integration | `./gradlew test` | ❌ W0 | ⬜ pending |
| 08-02-01 | 02 | 1 | AUTH/AUDIT | e2e | `npx playwright test` | ❌ W0 | ⬜ pending |
| 08-03-01 | 03 | 2 | INFRA-01 | manual | Docker build + tar | ✅ | ⬜ pending |
| 08-04-01 | 04 | 2 | INFRA-05 | manual | TLS cert verify | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `frontend/playwright.config.ts` — Playwright 설치 및 설정
- [ ] `backend/src/test/java/.../integration/` — 통합 테스트 디렉토리 구조
- [ ] `jmeter/` — JMeter 시나리오 디렉토리

*Existing infrastructure covers backend unit tests. E2E and performance test infra needs Wave 0 setup.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Docker tar 패키징 | INFRA-01 | Docker 이미지 빌드/save 환경 필요 | `docker build`, `docker save`, tar 크기 확인 |
| TLS 인증서 동작 | INFRA-05 | HTTPS 종단 검증은 실행 환경 필요 | `generate-cert.sh` 실행, HTTPS 접속 확인 |
| OWASP ZAP 스캔 | Security | ZAP Docker 실행 환경 필요 | ZAP Docker 기동, 스캔 실행, 리포트 확인 |
| JMeter 성능 테스트 | STAT-07 | 실행 환경 + DB 데이터 필요 | JMeter 시나리오 실행, 응답시간 < 5초 확인 |
| 배포 절차서 검증 | INFRA-01 | 문서 내용 검토 필요 | 절차서대로 실행하여 검증 |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 120s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
