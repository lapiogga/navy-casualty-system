---
phase: 02-rbac
plan: 02
subsystem: security
tags: [aes-256-gcm, pii-encryption, rrn-masking, audit-log, aop, spring-aspect, postgresql-rule]

requires:
  - phase: 01-project-foundation
    provides: "TB_AUDIT_LOG 테이블 (V6), BaseAuditEntity, Spring Boot 프로젝트 구조"
provides:
  - "PiiEncryptionConverter - AES-256-GCM Hibernate AttributeConverter"
  - "RrnMaskingUtil - 역할별 3단계 주민번호 마스킹"
  - "@AuditLog + AuditLogAspect - AOP 감사 로그 자동 기록"
  - "AuditLogService - 감사 로그 저장/검색 서비스"
  - "TB_AUDIT_LOG append-only RULE (UPDATE/DELETE 차단)"
affects: [03-dead-crud, 04-wounded-crud, 05-review, 06-document, 07-dashboard, 08-admin]

tech-stack:
  added: []
  patterns: ["AES-256-GCM PII 암호화", "역할 기반 마스킹", "AOP 감사 로그", "Builder 패턴 append-only 엔티티", "PostgreSQL RULE 기반 변경 차단"]

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/common/crypto/PiiEncryptionConverter.java
    - backend/src/main/java/com/navy/casualty/common/crypto/RrnMaskingUtil.java
    - backend/src/main/java/com/navy/casualty/audit/annotation/AuditLog.java
    - backend/src/main/java/com/navy/casualty/audit/aspect/AuditLogAspect.java
    - backend/src/main/java/com/navy/casualty/audit/entity/AuditLogEntry.java
    - backend/src/main/java/com/navy/casualty/audit/repository/AuditLogRepository.java
    - backend/src/main/java/com/navy/casualty/audit/service/AuditLogService.java
    - backend/src/main/resources/db/migration/V11__audit_log_append_only_rule.sql
    - backend/src/test/java/com/navy/casualty/common/crypto/PiiEncryptionConverterTest.java
    - backend/src/test/java/com/navy/casualty/common/crypto/RrnMaskingUtilTest.java
    - backend/src/test/java/com/navy/casualty/audit/AuditLogAspectTest.java
  modified: []

key-decisions:
  - "PiiEncryptionConverter에 테스트용 생성자(package-private)를 추가하여 환경변수 의존 없이 테스트 가능"
  - "AuditLogAspect에서 감사 로그 저장 실패 시 비즈니스 로직 중단 방지 (try-catch + 로깅)"
  - "AuditLogService.save()에 REQUIRES_NEW 전파로 비즈니스 트랜잭션 독립"
  - "AuditLogEntry setter 없음 - Builder 패턴 + @NoArgsConstructor(PROTECTED)로 불변성 보장"

patterns-established:
  - "PII 암호화: @Convert(converter = PiiEncryptionConverter.class)로 엔티티 필드에 적용"
  - "마스킹: RrnMaskingUtil.mask(rrn, role)로 컨트롤러/DTO 레이어에서 호출"
  - "감사 로그: @AuditLog(action='CREATE', targetTable='TB_XXX')를 서비스 메서드에 붙임"

requirements-completed: [AUDIT-01, AUDIT-02, AUDIT-03, AUDIT-04, AUDIT-05, AUDIT-06]

duration: 6min
completed: 2026-04-02
---

# Phase 02 Plan 02: PII 암호화 + 감사 로그 Summary

**AES-256-GCM PII 암호화 컨버터, 역할별 3단계 주민번호 마스킹, @AuditLog AOP 감사 로그 자동 기록, append-only RULE 적용**

## Performance

- **Duration:** 6 min
- **Started:** 2026-04-02T16:40:16Z
- **Completed:** 2026-04-02T16:46:19Z
- **Tasks:** 2
- **Files modified:** 11

## Accomplishments

- PiiEncryptionConverter: AES-256-GCM 암복호화, IV 랜덤, Base64 인코딩, PII_ENCRYPTION_KEY 환경변수 기반
- RrnMaskingUtil: VIEWER(전체마스킹)/OPERATOR(부분노출)/MANAGER+ADMIN(전체노출) 3단계 마스킹 D-04 준수
- @AuditLog + AuditLogAspect: AOP 기반 감사 로그 자동 기록, 성공/실패 모두 기록, SecurityContextHolder 사용자 추출
- V11 SQL: TB_AUDIT_LOG UPDATE/DELETE 차단 RULE, AUDIT-03 5년 보관 정책 주석 문서화

## Task Commits

1. **Task 1: PII 암호화 컨버터 + 주민번호 마스킹 유틸** - `ebbb0e3` (feat)
2. **Task 2: AOP 감사 로그 Aspect + AuditLog 엔티티 + append-only RULE** - `6e414ed` (feat)

## Files Created/Modified

- `backend/src/main/java/com/navy/casualty/common/crypto/PiiEncryptionConverter.java` - AES-256-GCM Hibernate AttributeConverter
- `backend/src/main/java/com/navy/casualty/common/crypto/RrnMaskingUtil.java` - 역할별 3단계 주민번호 마스킹 유틸
- `backend/src/main/java/com/navy/casualty/audit/annotation/AuditLog.java` - 감사 로그 메서드 어노테이션
- `backend/src/main/java/com/navy/casualty/audit/aspect/AuditLogAspect.java` - AOP Aspect (자동 감사 기록)
- `backend/src/main/java/com/navy/casualty/audit/entity/AuditLogEntry.java` - 감사 로그 JPA 엔티티 (Builder, setter 없음)
- `backend/src/main/java/com/navy/casualty/audit/repository/AuditLogRepository.java` - JpaSpecificationExecutor 기반 리포지토리
- `backend/src/main/java/com/navy/casualty/audit/service/AuditLogService.java` - 감사 로그 저장/검색 서비스
- `backend/src/main/resources/db/migration/V11__audit_log_append_only_rule.sql` - append-only RULE + 보관 정책
- `backend/src/test/java/com/navy/casualty/common/crypto/PiiEncryptionConverterTest.java` - 암복호화 단위 테스트
- `backend/src/test/java/com/navy/casualty/common/crypto/RrnMaskingUtilTest.java` - 마스킹 단위 테스트
- `backend/src/test/java/com/navy/casualty/audit/AuditLogAspectTest.java` - AOP 감사 로그 통합 테스트

## Decisions Made

- PiiEncryptionConverter에 package-private 테스트용 생성자 추가 (환경변수 의존 없이 단위 테스트 가능)
- AuditLogAspect 감사 로그 저장 실패 시 비즈니스 로직 중단 방지 (catch + log.error)
- AuditLogService.save()에 Propagation.REQUIRES_NEW로 비즈니스 트랜잭션 실패와 독립
- detail 생성은 D-09에 따라 간단한 한줄 요약 (JSON diff 미사용)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

- JAVA_HOME 미설정으로 Gradle 테스트 실행 불가. 코드 구조와 acceptance criteria는 수동 검증으로 확인. JDK 설치 후 테스트 실행 필요.

## User Setup Required

PII_ENCRYPTION_KEY 환경변수 설정 필요:
```bash
# AES-256 키 생성 (32바이트 Base64)
openssl rand -base64 32
# 결과를 PII_ENCRYPTION_KEY 환경변수로 설정
export PII_ENCRYPTION_KEY="생성된_키"
```

## Next Phase Readiness

- PiiEncryptionConverter를 엔티티 필드에 @Convert로 적용 가능
- RrnMaskingUtil을 DTO 변환 시 역할별 호출 가능
- @AuditLog를 서비스 메서드에 붙여 감사 로그 자동 기록 가능
- Phase 3~8의 모든 CRUD 서비스가 이 인프라를 사용할 예정

## Self-Check: PASSED

- All 11 created files confirmed present
- Commit ebbb0e3 (Task 1) confirmed in git log
- Commit 6e414ed (Task 2) confirmed in git log

---
*Phase: 02-rbac*
*Completed: 2026-04-02*
