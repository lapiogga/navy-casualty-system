---
phase: 03-dead-management
plan: 01
subsystem: dead-backend-api
tags: [dead, entity, service, controller, querydsl, api]
dependency_graph:
  requires: [01-01, 02-01, 02-02, 02-03]
  provides: [dead-crud-api, code-table-api, dead-entity, dead-service]
  affects: [03-02, 03-03]
tech_stack:
  added: []
  patterns: [record-dto, querydsl-dynamic-search, ssn-hash-dedup, status-state-machine]
key_files:
  created:
    - backend/src/main/java/com/navy/casualty/dead/entity/Dead.java
    - backend/src/main/java/com/navy/casualty/dead/entity/DeadStatus.java
    - backend/src/main/java/com/navy/casualty/dead/service/DeadService.java
    - backend/src/main/java/com/navy/casualty/dead/controller/DeadController.java
    - backend/src/main/java/com/navy/casualty/dead/repository/DeadRepositoryImpl.java
    - backend/src/main/java/com/navy/casualty/code/controller/CodeController.java
    - backend/src/main/resources/db/migration/V6__add_ssn_hash_column.sql
  modified:
    - backend/src/main/java/com/navy/casualty/common/exception/GlobalExceptionHandler.java
decisions:
  - "주민번호 해시(SHA-256) 별도 컬럼으로 중복 검증 (PiiEncryptionConverter IV 랜덤성 때문)"
  - "partial unique index로 논리 삭제된 레코드 제외"
  - "IllegalArgumentException -> 409, IllegalStateException -> 400 전역 매핑"
metrics:
  duration: 15min
  completed: "2026-04-03T01:24:00Z"
---

# Phase 3 Plan 1: 사망자 백엔드 REST API Summary

Dead 엔티티/서비스/컨트롤러 + 코드 테이블 엔티티/API 전체 구현. 군번/주민번호 이중 중복 검증(SHA-256 해시), REGISTERED->CONFIRMED->NOTIFIED 상태 전이, QueryDSL 8조건 동적 검색, @PreAuthorize 역할별 권한 가드 적용.

## What Was Built

### Task 1: Dead 엔티티/서비스/리포지토리 + 코드 테이블 (56abdf2)

- **코드 테이블 엔티티 5개**: RankCode, BranchCode, DeathType, DeathCode, UnitCode (BaseAuditEntity 상속, SQLRestriction)
- **코드 테이블 리포지토리 5개**: JpaRepository 인터페이스
- **DeadStatus enum**: REGISTERED->CONFIRMED->NOTIFIED 단방향 상태 전이
- **Dead 엔티티**: ssnHash 필드(SHA-256), PiiEncryptionConverter, updateStatus/update 메서드
- **DTO 5개**: DeadCreateRequest, DeadUpdateRequest, DeadSearchRequest, DeadResponse, DeadDeleteRequest (record 패턴)
- **DeadRepository**: existsByServiceNumber, existsBySsnHash 메서드
- **DeadRepositoryImpl**: QueryDSL BooleanBuilder 8조건 동적 검색
- **DeadService**: CRUD + 군번/주민번호 이중 중복 검증 + 논리 삭제 + 상태 전이 + @AuditLog
- **V6 마이그레이션**: ssn_hash 컬럼 + partial unique index
- **테스트**: DeadStatusTest 6 케이스 + DeadServiceTest 5 케이스 통과

### Task 2: DeadController/CodeController REST API + 통합 테스트 (449e49d)

- **DeadController**: GET / POST / PUT /{id} / DELETE /{id} / PUT /{id}/status
- **CodeController**: GET /ranks, /branches, /death-types, /death-codes, /units
- **GlobalExceptionHandler**: IllegalArgumentException->409, IllegalStateException->400 매핑 추가
- **DeadControllerTest**: 7 케이스 (등록 201, 군번중복 409, 주민번호중복 409, 검색 200, 권한부족 403, 삭제 200, 상태변경 200)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] PII_ENCRYPTION_KEY 환경변수 필요**
- **Found during:** Task 1 테스트
- **Issue:** Dead 엔티티의 @Convert(PiiEncryptionConverter)가 테스트 시 환경변수 필요
- **Fix:** 유효한 AES-256 키를 테스트 실행 시 환경변수로 전달
- **Files modified:** 없음 (환경 설정)

**2. [Rule 1 - Bug] softDelete 테스트에서 SQLRestriction으로 조회 불가**
- **Found during:** Task 1 테스트
- **Issue:** 논리 삭제 후 findAll()이 SQLRestriction에 의해 삭제된 레코드 필터링
- **Fix:** native query로 직접 조회하여 isDeleted() 검증
- **Files modified:** DeadServiceTest.java

## Known Stubs

None - 모든 API 엔드포인트가 실제 비즈니스 로직에 연결됨.

## Decisions Made

1. **주민번호 해시 컬럼 분리**: PiiEncryptionConverter가 매번 다른 IV를 사용하므로 동일 주민번호라도 암호화 결과가 다름. SHA-256 해시 별도 컬럼으로 중복 검증.
2. **partial unique index**: `WHERE deleted_at IS NULL`로 논리 삭제된 레코드는 중복 검증에서 제외.
3. **전역 예외 매핑**: IllegalArgumentException->409 CONFLICT (비즈니스 검증 실패), IllegalStateException->400 BAD_REQUEST (상태 전이 실패).

## Self-Check: PASSED

- 15개 핵심 파일 모두 존재 확인
- 커밋 56abdf2, 449e49d 모두 확인
