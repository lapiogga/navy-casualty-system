---
phase: 05-review-management
plan: 01
subsystem: api
tags: [jpa, querydsl, jsonb, spring-boot, flyway, cross-domain]

requires:
  - phase: 01-foundation
    provides: BaseAuditEntity, PiiEncryptionConverter, Flyway 인프라
  - phase: 02-auth-rbac
    provides: RBAC, AuditLog, SecurityContext
  - phase: 03-dead-management
    provides: Dead 엔티티, DeadRepository, DeadService 패턴
  - phase: 04-wounded-management
    provides: Wounded 엔티티, WoundedRepository, WoundType enum
provides:
  - Review/ReviewHistory JPA 엔티티
  - ReviewClassification 4-value enum (전공상/공상/기각/보류)
  - ReviewStatus 4단계 전이 enum
  - ReviewService (CRUD + 이력 스냅샷 + 자동반영 + 보훈청통보)
  - ReviewController REST API 7개 엔드포인트
  - Flyway V16 마이그레이션
affects: [05-02-PLAN, 05-03-PLAN]

tech-stack:
  added: []
  patterns: [JSONB 스냅샷 이력 저장, 크로스-도메인 자동반영]

key-files:
  created:
    - backend/src/main/java/com/navy/casualty/review/entity/Review.java
    - backend/src/main/java/com/navy/casualty/review/entity/ReviewHistory.java
    - backend/src/main/java/com/navy/casualty/review/entity/ReviewClassification.java
    - backend/src/main/java/com/navy/casualty/review/entity/ReviewStatus.java
    - backend/src/main/java/com/navy/casualty/review/service/ReviewService.java
    - backend/src/main/java/com/navy/casualty/review/controller/ReviewController.java
    - backend/src/main/resources/db/migration/V16__add_review_columns.sql
  modified:
    - backend/src/main/java/com/navy/casualty/dead/repository/DeadRepository.java
    - backend/src/main/java/com/navy/casualty/wounded/repository/WoundedRepository.java
    - backend/src/main/java/com/navy/casualty/dead/entity/Dead.java
    - backend/src/main/java/com/navy/casualty/wounded/entity/Wounded.java

key-decisions:
  - "심사 특성상 동일인 복수 심사 가능하므로 SSN 해시 중복 검증은 수행하지 않음 (군번+심사차수 중복만 검증)"
  - "classification 자동반영 시 상태 전이 불가하면 경고 로그 후 skip (트랜잭션 롤백 안 함)"
  - "Dead/Wounded에 findByServiceNumber, updateDeathType/updateWoundType 메서드 추가 (크로스-도메인 자동반영 필수)"

patterns-established:
  - "JSONB 스냅샷 이력: 수정 시 변경 전 상태를 ReviewSnapshot record로 직렬화하여 TB_REVIEW_HISTORY에 저장"
  - "크로스-도메인 자동반영: classification 변경 시 Dead/Wounded 레코드 자동 갱신 (단일 트랜잭션)"

requirements-completed: [REVW-01, REVW-02, REVW-03, REVW-04, REVW-06, REVW-07, REVW-08, AUDIT-07]

duration: 7min
completed: 2026-04-03
---

# Phase 05 Plan 01: 전공사상심사 백엔드 Summary

**Review/ReviewHistory 엔티티 + JSONB 스냅샷 이력 + Dead/Wounded 크로스-도메인 자동반영 + 보훈청 통보 REST API**

## Performance

- **Duration:** 7 min
- **Started:** 2026-04-03T03:50:35Z
- **Completed:** 2026-04-03T03:57:31Z
- **Tasks:** 2
- **Files modified:** 24

## Accomplishments
- Review/ReviewHistory JPA 엔티티와 ReviewClassification(4-value)/ReviewStatus(4단계 전이) enum 구현
- ReviewService에 이력 스냅샷 자동저장 + Dead/Wounded 크로스-도메인 자동반영 + 보훈청 통보 로직 구현
- ReviewController REST API 7개 엔드포인트 (CRUD + 이력조회 + 상태변경 + 보훈청통보)
- Flyway V16 마이그레이션 (ssn_hash, rank_id, branch_id, status 컬럼 추가)

## Task Commits

Each task was committed atomically:

1. **Task 1: Flyway V16 + Review/ReviewHistory 엔티티 + enum + DTO + Repository** - `9c78ea8` (feat)
2. **Task 2: ReviewService + ReviewController + Dead/Wounded 크로스-도메인 자동반영** - `b583b1d` (feat)

## Files Created/Modified
- `backend/src/main/resources/db/migration/V16__add_review_columns.sql` - TB_REVIEW 컬럼 추가 마이그레이션
- `backend/src/main/java/com/navy/casualty/review/entity/Review.java` - 전공사상심사 JPA 엔티티
- `backend/src/main/java/com/navy/casualty/review/entity/ReviewClassification.java` - 전공상/공상/기각/보류 4-value enum
- `backend/src/main/java/com/navy/casualty/review/entity/ReviewStatus.java` - 4단계 상태 전이 enum
- `backend/src/main/java/com/navy/casualty/review/entity/ReviewHistory.java` - JSONB 스냅샷 이력 엔티티
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewSnapshot.java` - 스냅샷 record DTO (ssn 제외)
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewCreateRequest.java` - 등록 요청 DTO
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewUpdateRequest.java` - 수정 요청 DTO
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewSearchRequest.java` - 검색 요청 DTO
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewResponse.java` - 응답 DTO
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewDeleteRequest.java` - 삭제 요청 DTO
- `backend/src/main/java/com/navy/casualty/review/dto/ReviewHistoryResponse.java` - 이력 응답 DTO
- `backend/src/main/java/com/navy/casualty/review/repository/ReviewRepository.java` - JPA 리포지토리
- `backend/src/main/java/com/navy/casualty/review/repository/ReviewRepositoryCustom.java` - 커스텀 검색 인터페이스
- `backend/src/main/java/com/navy/casualty/review/repository/ReviewRepositoryImpl.java` - QueryDSL 동적 검색 구현
- `backend/src/main/java/com/navy/casualty/review/repository/ReviewHistoryRepository.java` - 이력 리포지토리
- `backend/src/main/java/com/navy/casualty/review/service/ReviewService.java` - 비즈니스 로직
- `backend/src/main/java/com/navy/casualty/review/controller/ReviewController.java` - REST API 컨트롤러
- `backend/src/test/java/com/navy/casualty/review/ReviewClassificationTest.java` - enum 라벨 테스트
- `backend/src/test/java/com/navy/casualty/review/ReviewStatusTest.java` - 상태 전이 테스트
- `backend/src/main/java/com/navy/casualty/dead/repository/DeadRepository.java` - findByServiceNumber 추가
- `backend/src/main/java/com/navy/casualty/wounded/repository/WoundedRepository.java` - findByServiceNumber 추가
- `backend/src/main/java/com/navy/casualty/dead/entity/Dead.java` - updateDeathType 메서드 추가
- `backend/src/main/java/com/navy/casualty/wounded/entity/Wounded.java` - updateWoundType 메서드 추가

## Decisions Made
- 심사 특성상 동일인 복수 심사 가능하므로 SSN 해시 중복 검증은 수행하지 않음 (군번+심사차수 중복만 검증)
- classification 자동반영 시 상태 전이 불가하면 경고 로그 후 skip (트랜잭션 롤백 안 함)
- Dead/Wounded에 findByServiceNumber, updateDeathType/updateWoundType 메서드 추가 (크로스-도메인 자동반영 필수)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] DeadRepository/WoundedRepository에 findByServiceNumber 추가**
- **Found during:** Task 2 (ReviewService 구현)
- **Issue:** 크로스-도메인 자동반영에 필요한 findByServiceNumber 메서드가 기존 리포지토리에 없음
- **Fix:** DeadRepository, WoundedRepository에 Optional<T> findByServiceNumber(String) 추가
- **Files modified:** DeadRepository.java, WoundedRepository.java
- **Verification:** 컴파일 성공
- **Committed in:** b583b1d (Task 2 commit)

**2. [Rule 3 - Blocking] Dead.updateDeathType / Wounded.updateWoundType 메서드 추가**
- **Found during:** Task 2 (ReviewService 구현)
- **Issue:** 자동반영 로직에서 Dead/Wounded의 타입을 변경하는 메서드가 없음
- **Fix:** Dead에 updateDeathType(Long), Wounded에 updateWoundType(WoundType) 메서드 추가
- **Files modified:** Dead.java, Wounded.java
- **Verification:** 컴파일 성공
- **Committed in:** b583b1d (Task 2 commit)

---

**Total deviations:** 2 auto-fixed (2 blocking)
**Impact on plan:** 크로스-도메인 자동반영 기능에 필수적인 메서드 추가. 범위 확장 없음.

## Issues Encountered
None

## Known Stubs
None

## User Setup Required
None - 외부 서비스 설정 불필요.

## Next Phase Readiness
- 전공사상심사 백엔드 CRUD + 이력 + 자동반영 + 보훈청통보 완성
- 05-02-PLAN (프론트엔드 UI) 진행 가능
- 05-03-PLAN (Excel 내보내기) 진행 가능

## Self-Check: PASSED

- All 10 key files verified present
- Commit 9c78ea8 (Task 1) verified
- Commit b583b1d (Task 2) verified

---
*Phase: 05-review-management*
*Completed: 2026-04-03*
