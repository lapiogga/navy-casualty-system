---
phase: 04-wounded-management
plan: 01
subsystem: api
tags: [spring-boot, jpa, querydsl, rest-api, wounded]

requires:
  - phase: 01-foundation
    provides: BaseAuditEntity, PiiEncryptionConverter, 코드 테이블 엔티티/리포지토리, Flyway 마이그레이션 인프라
  - phase: 02-auth-rbac
    provides: SecurityConfig RBAC, AuditLog 어노테이션, ApiResponse 래퍼
provides:
  - Wounded 엔티티 + WoundedStatus 4단계 + WoundType 3-value enum
  - WoundedService CRUD + 중복 검증 + 상태 전이
  - WoundedController REST API 5개 엔드포인트
  - VeteransOffice 엔티티/리포지토리 + CodeController 보훈청 조회 API
  - QueryDSL WoundedRepositoryImpl 동적 검색
  - Flyway V15 ssn_hash 마이그레이션
affects: [04-wounded-management, frontend-wounded]

tech-stack:
  added: []
  patterns: [Dead 패턴 복제 -> Wounded 적용, 4단계 상태 전이 enum]

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/wounded/entity/Wounded.java
    - backend/src/main/java/com/navy/casualty/wounded/entity/WoundedStatus.java
    - backend/src/main/java/com/navy/casualty/wounded/entity/WoundType.java
    - backend/src/main/java/com/navy/casualty/wounded/service/WoundedService.java
    - backend/src/main/java/com/navy/casualty/wounded/controller/WoundedController.java
    - backend/src/main/java/com/navy/casualty/code/entity/VeteransOffice.java
    - backend/src/main/resources/db/migration/V15__add_wounded_ssn_hash_column.sql
  modified:
    - backend/src/main/java/com/navy/casualty/code/controller/CodeController.java

key-decisions:
  - "Dead 패턴 100% 복제 + 상이자 고유 필드(veteransOfficeId, diseaseName, woundType) 반영"
  - "WoundedStatus 4단계 전이: REGISTERED->UNDER_REVIEW->CONFIRMED->NOTIFIED (Dead의 3단계와 차별화)"

patterns-established:
  - "상이자 도메인: Dead와 동일한 엔티티-DTO-Repository-Service-Controller 계층 구조"
  - "코드 테이블 확장: VeteransOffice 엔티티 + CodeController 엔드포인트 추가 패턴"

requirements-completed: [WOND-01, WOND-02, WOND-03, WOND-04, WOND-06, WOND-07]

duration: 8min
completed: 2026-04-03
---

# Phase 4 Plan 1: Wounded Backend API Summary

**상이자 REST API 백엔드 전체 구현: 4단계 상태 전이, 3종 상이구분 enum, 군번/주민번호 이중 중복 검증, 보훈청 코드 조회 API**

## Performance

- **Duration:** 8 min
- **Started:** 2026-04-03T02:46:10Z
- **Completed:** 2026-04-03T02:54:00Z
- **Tasks:** 2
- **Files modified:** 19

## Accomplishments
- Wounded 엔티티 + WoundedStatus 4단계 상태 전이 + WoundType 3-value enum 완성
- WoundedService CRUD + 군번/주민번호 이중 중복 검증 + 감사 로그 적용
- WoundedController REST API 5개 엔드포인트 (검색/등록/수정/삭제/상태변경)
- VeteransOffice 엔티티 + CodeController /api/codes/veterans-offices 보훈청 조회
- QueryDSL 동적 검색 (8개 조건) + Flyway V15 ssn_hash 마이그레이션
- WoundedStatusTest + WoundedServiceTest 전체 통과

## Task Commits

1. **Task 1: Flyway + Wounded 엔티티/Enum/DTO/Repository + VeteransOffice** - `15e9bf3` (feat)
2. **Task 2: WoundedService + WoundedController + CodeController + ServiceTest** - `dee0968` (feat)

## Files Created/Modified
- `backend/src/main/resources/db/migration/V15__add_wounded_ssn_hash_column.sql` - ssn_hash 컬럼 + partial unique index
- `backend/src/main/java/com/navy/casualty/wounded/entity/Wounded.java` - 상이자 JPA 엔티티
- `backend/src/main/java/com/navy/casualty/wounded/entity/WoundedStatus.java` - 4단계 상태 전이 enum
- `backend/src/main/java/com/navy/casualty/wounded/entity/WoundType.java` - 3종 상이구분 enum
- `backend/src/main/java/com/navy/casualty/wounded/dto/WoundedCreateRequest.java` - 등록 요청 DTO
- `backend/src/main/java/com/navy/casualty/wounded/dto/WoundedUpdateRequest.java` - 수정 요청 DTO
- `backend/src/main/java/com/navy/casualty/wounded/dto/WoundedSearchRequest.java` - 검색 요청 DTO
- `backend/src/main/java/com/navy/casualty/wounded/dto/WoundedResponse.java` - 응답 DTO
- `backend/src/main/java/com/navy/casualty/wounded/dto/WoundedDeleteRequest.java` - 삭제 요청 DTO
- `backend/src/main/java/com/navy/casualty/wounded/repository/WoundedRepository.java` - JPA 리포지토리
- `backend/src/main/java/com/navy/casualty/wounded/repository/WoundedRepositoryCustom.java` - 커스텀 검색 인터페이스
- `backend/src/main/java/com/navy/casualty/wounded/repository/WoundedRepositoryImpl.java` - QueryDSL 동적 검색 구현
- `backend/src/main/java/com/navy/casualty/wounded/service/WoundedService.java` - 비즈니스 로직
- `backend/src/main/java/com/navy/casualty/wounded/controller/WoundedController.java` - REST API 컨트롤러
- `backend/src/main/java/com/navy/casualty/code/entity/VeteransOffice.java` - 보훈청 엔티티
- `backend/src/main/java/com/navy/casualty/code/repository/VeteransOfficeRepository.java` - 보훈청 리포지토리
- `backend/src/main/java/com/navy/casualty/code/controller/CodeController.java` - 보훈청 엔드포인트 추가
- `backend/src/test/java/com/navy/casualty/wounded/WoundedStatusTest.java` - 상태 전이 + WoundType 테스트
- `backend/src/test/java/com/navy/casualty/wounded/WoundedServiceTest.java` - 서비스 통합 테스트

## Decisions Made
- Dead 패턴 100% 복제 + 상이자 고유 필드 반영 (veteransOfficeId, diseaseName, woundType)
- WoundedStatus 4단계 전이 (Dead 3단계 대비 UNDER_REVIEW 추가)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Known Stubs
None

## Next Phase Readiness
- 상이자 백엔드 API 완성, 프론트엔드 UI 구현(04-02) 및 Excel 내보내기(04-03) 준비 완료
- VeteransOffice 코드 테이블 데이터 15건 이미 DB에 존재 (V9 마이그레이션)

## Self-Check: PASSED

- All 10 key files verified: FOUND
- Both commits verified: 15e9bf3, dee0968

---
*Phase: 04-wounded-management*
*Completed: 2026-04-03*
