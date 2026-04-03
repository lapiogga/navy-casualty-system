---
phase: 03-dead-management
verified: 2026-04-03T01:51:43Z
status: passed
score: 11/11 must-haves verified
re_verification: false
---

# Phase 3: 사망자 관리 Verification Report

**Phase Goal:** 사망자 정보의 등록·수정·삭제·조회와 Excel 내보내기를 완성한다.
**Verified:** 2026-04-03T01:51:43Z
**Status:** passed
**Re-verification:** No — initial verification

---

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|---------|
| 1 | GET /api/dead?name=홍길동&page=0&size=10 이 검색 조건에 맞는 사망자 목록을 페이징으로 반환한다 | VERIFIED | DeadController.search() + DeadRepositoryImpl.search() QueryDSL 8조건 BooleanBuilder 구현 확인 |
| 2 | POST /api/dead 로 사망자를 등록하면 201과 DeadResponse를 반환한다 | VERIFIED | DeadController.create() @PreAuthorize("hasRole('OPERATOR')") + HttpStatus.CREATED 반환 확인 |
| 3 | PUT /api/dead/{id} 로 사망자 정보를 수정할 수 있다 | VERIFIED | DeadController.update() + DeadService.update() + Dead.update() 메서드 체인 확인 |
| 4 | DELETE /api/dead/{id} 로 논리 삭제 시 삭제 사유가 필수이다 | VERIFIED | DeadDeleteRequest record + @Valid + DeadService.softDelete(id, reason) 확인 |
| 5 | 동일 군번으로 중복 등록 시 409 에러를 반환한다 | VERIFIED | DeadService.create()에서 existsByServiceNumber() 검사 + GlobalExceptionHandler 409 매핑 확인 |
| 6 | 동일 주민번호(해시 기준)로 중복 등록 시 409 에러를 반환한다 | VERIFIED | hashSsn(SHA-256) + existsBySsnHash() 중복 검사 + V6 마이그레이션 partial unique index 확인 |
| 7 | 상태 전이가 REGISTERED->CONFIRMED->NOTIFIED 순서로만 가능하다 | VERIFIED | DeadStatus.canTransitionTo() switch 구현 + DeadStatusTest 6케이스 확인 |
| 8 | 코드 테이블 API(/api/codes/*)가 Select 드롭다운용 데이터를 반환한다 | VERIFIED | CodeController 5개 엔드포인트(ranks/branches/death-types/death-codes/units) 구현 확인 |
| 9 | 사망자 관리 메뉴 클릭 시 /dead 경로에서 DeadListPage가 렌더링된다 | VERIFIED | routes/index.tsx: { path: 'dead', element: <DeadListPage /> } PlaceholderPage 교체 완료 |
| 10 | GET /api/dead/excel 이 검색 조건 적용된 Excel 파일을 다운로드한다 | VERIFIED | DeadController.exportExcel() @GetMapping("/excel") + DeadExcelService.exportExcel() SXSSFWorkbook 구현 확인 |
| 11 | 프론트엔드 Excel 다운로드 버튼이 Blob으로 파일을 받아 다운로드 트리거한다 | VERIFIED | useExportDeadExcel() responseType:'blob' + createObjectURL + link.click() 확인. DeadListPage 버튼 isPending 로딩 상태 연결 확인 |

**Score:** 11/11 truths verified

---

### Required Artifacts

| Artifact | Status | Evidence |
|----------|--------|---------|
| `backend/src/main/java/com/navy/casualty/dead/entity/Dead.java` | VERIFIED | @Entity, @Convert(PiiEncryptionConverter), extends BaseAuditEntity, ssnHash 필드, updateStatus/update 메서드 존재 |
| `backend/src/main/java/com/navy/casualty/dead/entity/DeadStatus.java` | VERIFIED | REGISTERED/CONFIRMED/NOTIFIED enum + canTransitionTo() 단방향 전이 로직 |
| `backend/src/main/java/com/navy/casualty/dead/repository/DeadRepositoryImpl.java` | VERIFIED | QueryDSL BooleanBuilder 8조건 동적 검색 + searchAll() 페이징 없는 전체 조회 |
| `backend/src/main/java/com/navy/casualty/dead/repository/DeadRepository.java` | VERIFIED | existsByServiceNumber(), existsBySsnHash() 메서드 정의 |
| `backend/src/main/java/com/navy/casualty/dead/service/DeadService.java` | VERIFIED | @AuditLog 4개 메서드, SHA-256 해시 이중 중복 검증, 역할별 주민번호 마스킹, 상태 전이 |
| `backend/src/main/java/com/navy/casualty/dead/controller/DeadController.java` | VERIFIED | @RestController, CRUD 5개 + /excel 엔드포인트, @PreAuthorize 역할 가드 |
| `backend/src/main/java/com/navy/casualty/dead/service/DeadExcelService.java` | VERIFIED | SXSSFWorkbook(100), HEADERS 15컬럼, RrnMaskingUtil.mask(), @AuditLog(EXPORT) |
| `backend/src/main/java/com/navy/casualty/code/controller/CodeController.java` | VERIFIED | 5개 @GetMapping 엔드포인트, 각 코드 리포지토리에서 실 데이터 조회 |
| `backend/src/main/resources/db/migration/V6__add_ssn_hash_column.sql` | VERIFIED | ssn_hash VARCHAR(64) 컬럼 추가 + partial unique index (deleted_at IS NULL) |
| `backend/build.gradle.kts` | VERIFIED | `implementation("org.apache.poi:poi-ooxml:5.3.0")` line 31 확인 |
| `frontend/src/types/dead.ts` | VERIFIED | DeadRecord, DeadSearchParams, DeadCreateForm, DeadUpdateForm, RankCode, BranchCode, DeathType, DeathCode, UnitCode, PageData 전체 정의 |
| `frontend/src/utils/rrnValidation.ts` | VERIFIED | validateRrn() 가중치 [2,3,4,5,6,7,8,9,2,3,4,5] mod 11 알고리즘 구현 |
| `frontend/src/api/dead.ts` | VERIFIED | useDeadList, useCreateDead, useUpdateDead, useDeleteDead, useUpdateDeadStatus, useExportDeadExcel + 코드 5개 훅. any 타입 없음, axios.isAxiosError 타입 가드 사용 |
| `frontend/src/pages/dead/DeadListPage.tsx` | VERIFIED | 검색 폼 8필드, searchExpanded 접기/펼치기, setPage(1) 조회 리셋, Ant Design Table 서버사이드 페이징, Excel 다운로드 버튼(isPending 로딩) |
| `frontend/src/pages/dead/DeadFormModal.tsx` | VERIFIED | validateRrn() 체크섬 검증, useCreateDead/useUpdateDead 연결, 주민번호 입력 시 생년월일 자동 추출, 수정 모드 군번 disabled |
| `frontend/src/pages/dead/DeadDeleteModal.tsx` | VERIFIED | useDeleteDead(), 삭제 사유 TextArea 필수 입력 |
| `frontend/src/routes/index.tsx` | VERIFIED | { path: 'dead', element: <DeadListPage /> } — PlaceholderPage 교체 완료 |

---

### Key Link Verification

| From | To | Via | Status | Evidence |
|------|----|-----|--------|---------|
| DeadController | DeadService | DI injection | WIRED | `private final DeadService deadService` 확인 |
| DeadController | DeadExcelService | DI injection | WIRED | `private final DeadExcelService deadExcelService` 확인 |
| DeadService | DeadRepositoryImpl | QueryDSL search | WIRED | `deadRepository.search(request, pageable)` 호출 확인 |
| DeadService | DeadRepository | 주민번호 해시 중복 검증 | WIRED | `deadRepository.existsBySsnHash(ssnHash)` 호출 확인 |
| Dead.java | BaseAuditEntity | extends | WIRED | `public class Dead extends BaseAuditEntity` 확인 |
| Dead.java | PiiEncryptionConverter | @Convert annotation | WIRED | `@Convert(converter = PiiEncryptionConverter.class)` 확인 |
| DeadExcelService | RrnMaskingUtil | 주민번호 마스킹 | WIRED | `RrnMaskingUtil.mask(dead.getSsnEncrypted(), userRole)` 확인 |
| DeadExcelService | DeadRepository.searchAll | 전체 조회 | WIRED | `deadRepository.searchAll(request)` 확인 |
| DeadListPage.tsx | api/dead.ts useDeadList | 목록 데이터 조회 | WIRED | `useDeadList({ ...searchParams, page: page-1, size: pageSize })` 확인 |
| DeadFormModal.tsx | api/dead.ts useCreateDead/useUpdateDead | 등록/수정 | WIRED | `createDead.mutate(payload)` / `updateDead.mutate({id, data})` 확인 |
| DeadDeleteModal.tsx | api/dead.ts useDeleteDead | 삭제 | WIRED | `deleteDead.mutate({ id: recordId, reason })` 확인 |
| DeadListPage.tsx | api/dead.ts useExportDeadExcel | Excel 다운로드 | WIRED | `exportExcel.mutate(searchParams)` 확인 |
| routes/index.tsx | DeadListPage.tsx | route definition | WIRED | `{ path: 'dead', element: <DeadListPage /> }` 확인 |

---

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|--------------|--------|--------------------|--------|
| DeadListPage.tsx | data (PageData<DeadRecord>) | useDeadList -> GET /api/dead -> DeadService.search() -> DeadRepositoryImpl QueryDSL | DB 조회 (QueryDSL selectFrom(dead)) | FLOWING |
| DeadExcelService.java | list (List<Dead>) | deadRepository.searchAll() -> DeadRepositoryImpl.searchAll() -> QueryDSL | DB 조회 (queryFactory.selectFrom(dead).fetch()) | FLOWING |
| CodeController.java | 5개 코드 목록 | 각 코드 Repository.findAll() | DB 조회 | FLOWING |

---

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|---------|
| DEAD-01 | 03-01, 03-02 | 사망자 목록 조회 (군구분·군번·성명·생년월일·계급·소속 복합 검색) | SATISFIED | DeadRepositoryImpl 8조건 BooleanBuilder + DeadListPage 검색 폼 8필드 |
| DEAD-02 | 03-01, 03-02 | 사망자 정보 등록 (군번, 성명, 주민번호, 계급, 소속 등) | SATISFIED | POST /api/dead + DeadFormModal (모든 필드 포함) |
| DEAD-03 | 03-01, 03-02 | 사망자 정보 수정 | SATISFIED | PUT /api/dead/{id} + DeadFormModal isEdit 모드 |
| DEAD-04 | 03-01, 03-02 | 사망자 논리 삭제 (삭제 사유 필수) | SATISFIED | DELETE /api/dead/{id} + DeadDeleteModal 사유 필수 입력 |
| DEAD-05 | 03-03 | Excel 다운로드 (검색 조건 적용, 주민번호 마스킹) | SATISFIED | GET /api/dead/excel + SXSSFWorkbook + RrnMaskingUtil.mask() + useExportDeadExcel |
| DEAD-06 | 03-01, 03-02 | 사망 기록 상태 관리 (등록->확정->보훈청통보완료) | SATISFIED | DeadStatus enum canTransitionTo() + PUT /api/dead/{id}/status + DeadListPage 상태 버튼 |
| DEAD-07 | 03-01 | 중복 등록 방지 (군번 또는 주민번호 기준) | SATISFIED | existsByServiceNumber() + existsBySsnHash(SHA-256) + V6 partial unique index |

**모든 7개 요구사항 SATISFIED.**

---

### Anti-Patterns Found

특이사항 없음. 스캔 결과:

- `dead.ts`: `any` 타입 없음 (axios.isAxiosError 타입 가드 사용)
- `DeadListPage.tsx`: Excel 버튼에 `disabled` 없음 (실제 기능 연결됨)
- `DeadExcelService.java`: `@AuditLog(action = "EXPORT")` 적용됨
- `DeadService.java`: `@AuditLog` CREATE/UPDATE/DELETE 3개 모두 적용됨

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| (없음) | - | - | - | - |

---

### Behavioral Spot-Checks

서버를 기동하지 않고 정적 분석으로만 검증. 런타임 동작은 인간 검증 항목으로 이관.

| Behavior | Check | Status |
|----------|-------|--------|
| DeadStatus 전이 규칙 | DeadStatusTest 6케이스 코드 존재 확인 | PASS |
| 군번 중복 시 409 | DeadServiceTest create_duplicateServiceNumber_throws 코드 존재 확인 | PASS |
| 주민번호 해시 중복 시 409 | DeadServiceTest create_duplicateSsnHash_throws 코드 존재 확인 | PASS |
| Excel 헤더 15컬럼 | DeadExcelServiceTest exportExcel_containsHeaderRow 코드 존재 확인 | PASS |
| validateRrn() 함수 | rrnValidation.ts 가중치/mod11 로직 코드 존재 확인 | PASS |

---

### Human Verification Required

#### 1. 사망자 등록/수정/삭제 E2E 흐름

**Test:** 브라우저에서 /dead 접근 후 등록 버튼 클릭 -> 주민번호 입력(유효/무효) -> 등록 -> 목록 자동 갱신 확인
**Expected:** 주민번호 체크섬 실패 시 폼 오류 메시지, 등록 성공 후 목록에 새 행 추가
**Why human:** 브라우저 렌더링 + 실제 API 호출 흐름은 정적 분석으로 검증 불가

#### 2. 검색 폼 접기/펼치기 + 1페이지 리셋 동작

**Test:** 검색 조건 입력 후 2페이지 이동 -> 검색 폼 조건 변경 후 조회 클릭 -> 1페이지로 리셋 확인
**Expected:** 조회 버튼 클릭 시 항상 1페이지로 초기화
**Why human:** 페이지 상태 변경은 런타임 React 상태 동작

#### 3. 역할별 주민번호 마스킹 표시

**Test:** VIEWER 계정으로 사망자 목록 조회 -> 주민번호 전체 마스킹 확인. MANAGER 계정으로 조회 -> 전체 노출 확인
**Expected:** VIEWER: 전체 마스킹, OPERATOR: 뒷자리 마스킹, MANAGER+: 전체 노출
**Why human:** 역할별 응답 내용 차이는 실제 인증된 사용자로 테스트 필요

#### 4. Excel 다운로드 파일 내용 검증

**Test:** 검색 조건 입력 후 Excel 다운로드 버튼 클릭 -> 파일 열어서 주민번호 마스킹 및 검색 조건 반영 여부 확인
**Expected:** 검색 조건에 해당하는 행만 포함, 주민번호 역할별 마스킹 적용
**Why human:** 실제 파일 다운로드 및 내용 확인은 브라우저/오피스 도구 필요

---

## Summary

Phase 3 목표인 사망자 정보의 등록·수정·삭제·조회와 Excel 내보내기가 코드베이스 수준에서 완전히 구현되었다.

**백엔드:**
- Dead 엔티티(PiiEncryptionConverter + ssnHash 이중 보호), DeadStatus 상태 전이 머신, QueryDSL 8조건 동적 검색, 군번/주민번호 이중 중복 방지가 모두 구현됨
- DeadExcelService: SXSSFWorkbook 스트리밍 방식 + 역할별 주민번호 마스킹 + @AuditLog(EXPORT)
- 4개 테스트 파일(DeadStatusTest, DeadServiceTest, DeadControllerTest, DeadExcelServiceTest) 존재

**프론트엔드:**
- 모든 타입, API 훅(11개), 검색 폼(8필드+접기/펼치기), 서버사이드 Table, 등록/수정/삭제 Modal, Excel 다운로드 버튼 완성
- validateRrn() 체크섬 검증 모든 입력 경로에 연결됨
- 라우팅: /dead -> DeadListPage (PlaceholderPage 완전 교체)

DEAD-01~07 7개 요구사항 전체 SATISFIED. 런타임 E2E 검증 4항목은 인간 테스트 필요.

---

_Verified: 2026-04-03T01:51:43Z_
_Verifier: Claude (gsd-verifier)_
