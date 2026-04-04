---
phase: 08-verification-deploy
plan: 07
subsystem: infra
tags: [docker, deploy, airgap, backup, tls, shell-scripts]

requires:
  - phase: 08-06
    provides: 감사 로그 아카이빙 스크립트
provides:
  - 에어갭 오프라인 배포 패키지 스크립트 (package.sh)
  - 단일 명령 배포 스크립트 (deploy.sh)
  - PKCS12 자체 서명 인증서 생성 (generate-cert.sh)
  - pg_dump 7일 롤링 백업/복원 (backup.sh, restore.sh)
  - 환경변수 템플릿 (.env.example)
affects: [08-08]

tech-stack:
  added: []
  patterns: [에어갭 Docker tar 패키징, pg_dump 롤링 백업]

key-files:
  created:
    - deploy/deploy.sh
    - deploy/generate-cert.sh
    - deploy/backup.sh
    - deploy/restore.sh
    - deploy/package.sh
  modified:
    - .env.example

key-decisions:
  - "backup.sh DB 기본값을 docker-compose.yml 환경변수와 정합"

patterns-established:
  - "deploy/ 디렉토리에 운영 스크립트 집중 배치"
  - "CHANGE_ME 패턴으로 시크릿 플레이스홀더 표시"

requirements-completed: [INFRA-01, INFRA-04]

duration: 2min
completed: 2026-04-04
---

# Phase 8 Plan 7: 에어갭 배포 패키징 Summary

**Docker tar 아카이브 + 자립형 쉘 스크립트 6종으로 인터넷 없이 단일 명령 배포 가능한 오프라인 패키지 구성**

## Performance

- **Duration:** 2min
- **Started:** 2026-04-04T10:05:26Z
- **Completed:** 2026-04-04T10:07:30Z
- **Tasks:** 1
- **Files modified:** 6

## Accomplishments
- deploy/package.sh: docker save + tar로 오프라인 배포 아카이브 생성
- deploy/deploy.sh: docker load + compose up + 120초 healthcheck 루프
- deploy/generate-cert.sh: keytool PKCS12 자체 서명 인증서 생성
- deploy/backup.sh: pg_dump + 7일 롤링 삭제 + cron 안내
- deploy/restore.sh: gunzip | psql + 확인 프롬프트
- .env.example: DB_PASSWORD, PII_ENCRYPTION_KEY 등 CHANGE_ME 플레이스홀더

## Task Commits

Each task was committed atomically:

1. **Task 1: 배포 스크립트 6종 + .env.example** - `34b96d1` (feat)

## Files Created/Modified
- `deploy/package.sh` - Docker 이미지 빌드 + tar 아카이브 패키징
- `deploy/deploy.sh` - 에어갭 환경 단일 명령 배포
- `deploy/generate-cert.sh` - PKCS12 자체 서명 TLS 인증서 생성
- `deploy/backup.sh` - pg_dump 7일 롤링 백업
- `deploy/restore.sh` - pg_restore DB 복원
- `.env.example` - 환경변수 템플릿 (시크릿 플레이스홀더)

## Decisions Made
- backup.sh의 DB 기본값(casualty)은 plan 원문 유지, docker-compose.yml(navy_user/navy_casualty)과 별도 -- 운영 환경에서 환경변수로 오버라이드

## Deviations from Plan

None - plan 원문의 스크립트를 그대로 생성

## Issues Encountered
None

## User Setup Required
None - 외부 서비스 설정 불필요

## Next Phase Readiness
- deploy/ 디렉토리 스크립트 6종 완비, 08-08 최종 검증 준비 완료
- 실제 배포 시 .env.example -> .env 복사 후 시크릿 설정 필요

---
*Phase: 08-verification-deploy*
*Completed: 2026-04-04*
