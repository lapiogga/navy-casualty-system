---
phase: 04-wounded-management
verified: 2026-04-03T03:16:04Z
status: passed
score: 13/13 must-haves verified
re_verification: false
gaps: []
human_verification:
  - test: "브라우저에서 /wounded 접속 후 검색 폼 접기/펼치기 버튼 동작 확인"
    expected: "Card 토글 시 검색 필드 영역이 숨겨지고 다시 나타남"
    why_human: "UI 인터랙션은 코드 정적 분석으로 검증 불가"
  - test: "등록 Modal에서 보훈청 Select 검색(showSearch) 동작 확인"
    expected: "타이핑 시 보훈청/보훈지청 목록이 필터링됨"
    why_human: "Ant Design Select showSearch 실제 렌더링 동작은 브라우저 확인 필요"
  - test: "Excel 다운로드 후 파일 내 주민번호 마스킹 상태 확인"
    expected: "역할(VIEWER/OPERATOR/MANAGER)별 마스킹 정도가 다르게 표시됨"
    why_human: "실제 로그인 사용자 역할을 바꿔가며 다운로드 비교 필요"
---

# Phase 4: Wounded Management Verification Report

**Phase Goal:** 상이자 정보의 등록·수정·삭제·조회와 Excel 내보내기를 완성한다. Phase 3 패턴을 재활용한다.
**Verified:** 2026-04-03T03:16:04Z
**Status:** passed
**Re-verification:** No — initial verification

---

## Goal Achievement

### Observable Truths

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | GET /api/wounded 검색 + 페이징 반환 | VERIFIED | WoundedController.search() + WoundedRepositoryImpl.search() BooleanBuilder 8개 조건 |
| 2 | POST /api/wounded 등록 시 201 + WoundedResponse 반환 | VERIFIED | WoundedController.create() → `ResponseEntity.status(HttpStatus.CREATED)` |
| 3 | PUT /api/wounded/{id} 수정 | VERIFIED | WoundedController.update() + Wounded.update(request) |
| 4 | DELETE /api/wounded/{id} 논리 삭제 + 사유 필수 | VERIFIED | WoundedController.delete() + WoundedDeleteRequest(@NotBlank reason) + softDelete() |
| 5 | 동일 군번 중복 등록 시 409(IllegalArgumentException) | VERIFIED | WoundedService.create() existsByServiceNumber 검증 |
| 6 | 동일 주민번호 해시 중복 등록 시 409(IllegalArgumentException) | VERIFIED | WoundedService.create() existsBySsnHash(hashSsn(ssn)) 검증 |
| 7 | 상태 전이 REGISTERED→UNDER_REVIEW→CONFIRMED→NOTIFIED 순서만 가능 | VERIFIED | WoundedStatus.canTransitionTo() switch 4단계 + WoundedStatusTest 7개 케이스 |
| 8 | GET /api/codes/veterans-offices 보훈청 목록 반환 | VERIFIED | CodeController.getVeteransOffices() + VeteransOfficeRepository |
| 9 | 상이구분 COMBAT_WOUND/DUTY_WOUND/GENERAL_WOUND 3-value enum | VERIFIED | WoundType.java COMBAT_WOUND("전공상"), DUTY_WOUND("공상"), GENERAL_WOUND("일반상이") |
| 10 | /wounded 경로에서 WoundedListPage 렌더링 | VERIFIED | routes/index.tsx line 24: `{ path: 'wounded', element: <WoundedListPage /> }` |
| 11 | 검색 폼 8개 필드 + 접기/펼치기 | VERIFIED | WoundedListPage.tsx searchExpanded state + 8개 Form.Item (branchId/serviceNumber/name/birthDate/rankId/unitId/woundType/status) |
| 12 | Excel 내보내기 GET /api/wounded/excel | VERIFIED | WoundedController.exportExcel() + WoundedExcelService SXSSFWorkbook 15컬럼 (보훈청명/병명/상이구분 포함) |
| 13 | 프론트엔드 Excel 다운로드 Blob 처리 | VERIFIED | wounded.ts useExportWoundedExcel() responseType:'blob' + createObjectURL 다운로드 트리거 |

**Score:** 13/13 truths verified

---

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `backend/.../wounded/entity/Wounded.java` | JPA 엔티티 (@Entity, ssnHash, veteransOfficeId, diseaseName, woundType) | VERIFIED | @Entity, @Table(TB_WOUNDED), @SQLRestriction, extends BaseAuditEntity, @Convert(PiiEncryptionConverter) 모두 확인 |
| `backend/.../wounded/entity/WoundedStatus.java` | 4단계 상태 전이 enum | VERIFIED | REGISTERED/UNDER_REVIEW/CONFIRMED/NOTIFIED + canTransitionTo() switch |
| `backend/.../wounded/entity/WoundType.java` | 3-value enum | VERIFIED | COMBAT_WOUND("전공상"), DUTY_WOUND("공상"), GENERAL_WOUND("일반상이") |
| `backend/.../wounded/repository/WoundedRepositoryImpl.java` | QueryDSL 동적 검색 | VERIFIED | BooleanBuilder 8개 조건 + search() + searchAll() |
| `backend/.../wounded/service/WoundedService.java` | 비즈니스 로직 CRUD + 중복검증 + 상태전이 | VERIFIED | @AuditLog CREATE/UPDATE/DELETE, existsByServiceNumber, existsBySsnHash, hashSsn, RrnMaskingUtil.mask, veteransOfficeRepository |
| `backend/.../wounded/controller/WoundedController.java` | REST API 5개 엔드포인트 | VERIFIED | GET/POST/PUT/DELETE/PUT(status) + GET(/excel) |
| `backend/.../code/entity/VeteransOffice.java` | 보훈청 JPA 엔티티 | VERIFIED | @Entity, @Table(TB_VETERANS_OFFICE), officeName, officeType |
| `backend/.../wounded/service/WoundedExcelService.java` | SXSSFWorkbook Excel (보훈청/병명/상이구분) | VERIFIED | SXSSFWorkbook(100), HEADERS 15개, RrnMaskingUtil.mask, @AuditLog(EXPORT) |
| `frontend/src/types/wounded.ts` | WoundedRecord, WoundedSearchParams, WoundedCreateForm, WoundedUpdateForm | VERIFIED | 모든 타입 정의 + VeteransOffice + WoundType |
| `frontend/src/api/wounded.ts` | react-query 훅 7개 | VERIFIED | useWoundedList, useCreateWounded, useUpdateWounded, useDeleteWounded, useUpdateWoundedStatus, useVeteransOffices, useExportWoundedExcel |
| `frontend/src/pages/wounded/WoundedListPage.tsx` | 목록 + 검색 폼 | VERIFIED | useWoundedList, searchExpanded, COMBAT_WOUND/DUTY_WOUND/GENERAL_WOUND, UNDER_REVIEW, veteransOfficeName, diseaseName, woundTypeName, useExportWoundedExcel |
| `frontend/src/pages/wounded/WoundedFormModal.tsx` | 등록/수정 Modal | VERIFIED | validateRrn, useCreateWounded, useUpdateWounded, useVeteransOffices, veteransOfficeId, diseaseName, woundType |
| `frontend/src/pages/wounded/WoundedDeleteModal.tsx` | 삭제 Modal (사유 필수) | VERIFIED | useDeleteWounded, required TextArea |
| `backend/db/migration/V15__add_wounded_ssn_hash_column.sql` | ssn_hash 컬럼 + partial unique index | VERIFIED | ALTER TABLE + idx_wounded_ssn_hash WHERE deleted_at IS NULL |

---

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| WoundedController | WoundedService | DI injection | VERIFIED | `private final WoundedService woundedService` |
| WoundedController | WoundedExcelService | DI injection | VERIFIED | `private final WoundedExcelService woundedExcelService` |
| WoundedService | WoundedRepositoryImpl | QueryDSL search | VERIFIED | `woundedRepository.search(request, pageable)` |
| WoundedService | WoundedRepository | 주민번호 해시 중복 검증 | VERIFIED | `woundedRepository.existsBySsnHash(ssnHash)` |
| Wounded.java | BaseAuditEntity | extends | VERIFIED | `public class Wounded extends BaseAuditEntity` |
| Wounded.java | PiiEncryptionConverter | @Convert annotation | VERIFIED | `@Convert(converter = PiiEncryptionConverter.class)` on ssnEncrypted |
| CodeController | VeteransOfficeRepository | 보훈청 코드 조회 API | VERIFIED | `veteransOfficeRepository.findAll()` in getVeteransOffices() |
| WoundedExcelService | RrnMaskingUtil | 주민번호 마스킹 | VERIFIED | `RrnMaskingUtil.mask(w.getSsnEncrypted(), userRole)` |
| WoundedExcelService | VeteransOfficeRepository | 보훈청명 조회 | VERIFIED | `officeMap = veteransOfficeRepository.findAll().stream()...` |
| WoundedListPage.tsx | api/wounded.ts | useWoundedList hook | VERIFIED | import + `useWoundedList({ ...searchParams, page: page-1, size: pageSize })` |
| WoundedFormModal.tsx | api/wounded.ts | useCreateWounded / useUpdateWounded | VERIFIED | 양쪽 훅 import + mutate() 호출 확인 |
| WoundedFormModal.tsx | api/wounded.ts | useVeteransOffices | VERIFIED | `const { data: veteransOffices } = useVeteransOffices()` |
| routes/index.tsx | WoundedListPage.tsx | route definition | VERIFIED | `{ path: 'wounded', element: <WoundedListPage /> }` — PlaceholderPage 교체 완료 |
| WoundedListPage.tsx | api/wounded.ts | useExportWoundedExcel | VERIFIED | `const exportExcel = useExportWoundedExcel()` + `onClick={() => exportExcel.mutate(searchParams)}` |

---

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|--------------------|--------|
| WoundedListPage.tsx | `data` (Page<WoundedRecord>) | useWoundedList → GET /api/wounded | WoundedRepositoryImpl.search() → DB query | FLOWING |
| WoundedFormModal.tsx | `veteransOffices` (VeteransOffice[]) | useVeteransOffices → GET /api/codes/veterans-offices | VeteransOfficeRepository.findAll() → DB query | FLOWING |
| WoundedExcelService | `list` (List<Wounded>) | woundedRepository.searchAll(request) | WoundedRepositoryImpl.searchAll() → JPAQueryFactory | FLOWING |

---

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| WoundedStatus enum 상태 전이 로직 | WoundedStatusTest.java (7개 테스트) | REGISTERED.canTransitionTo(UNDER_REVIEW)=true, 건너뛰기=false 코드 확인 | PASS |
| WoundType enum 라벨 | WoundType.java COMBAT_WOUND("전공상") | 3값 enum + getLabel() 확인 | PASS |
| WoundedService 중복 검증 | WoundedServiceTest.java 코드 직접 확인 | existsByServiceNumber + existsBySsnHash + @WithMockUser 테스트 | PASS |
| Excel 헤더 구성 | WoundedExcelService.HEADERS 배열 | "보훈청명"[11], "병명"[12], "상이구분"[13] 위치 확인 | PASS |
| 라우팅 PlaceholderPage 제거 | routes/index.tsx grep | `PlaceholderPage.*상이자` 패턴 없음, WoundedListPage로 교체 완료 | PASS |
| TypeScript 타입 안전성 | api/wounded.ts `any` 타입 사용 여부 | isAxiosError 타입 가드 사용, `any` 없음 | PASS |

Step 7b: 서버 미실행 상태이므로 API 직접 호출 테스트는 SKIP.

---

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|-------------|------------|-------------|--------|----------|
| WOND-01 | 04-01, 04-02 | 상이자 목록 조회 (군구분·군번·성명·생년월일·계급·소속 복합 검색) | SATISFIED | WoundedRepositoryImpl 8조건 BooleanBuilder + WoundedListPage 8개 검색 필드 |
| WOND-02 | 04-01, 04-02 | 상이자 등록 (군번, 성명, 주민번호, 계급, 소속, 입대일자, 전화번호, 현주소, 보훈청명, 병명, 상이구분) | SATISFIED | WoundedCreateRequest 전 필드 + WoundedFormModal 모든 Input/Select 포함 |
| WOND-03 | 04-01, 04-02 | 상이자 수정 | SATISFIED | WoundedController.update() + Wounded.update(request) + WoundedFormModal 수정 모드 |
| WOND-04 | 04-01, 04-02 | 논리 삭제 (사유 필수) | SATISFIED | WoundedDeleteRequest @NotBlank reason + WoundedDeleteModal required TextArea |
| WOND-05 | 04-03 | Excel 다운로드 (검색 조건 적용, 주민번호 마스킹) | SATISFIED | WoundedExcelService.exportExcel() + useExportWoundedExcel Blob 처리 + WoundedListPage 버튼 활성화 |
| WOND-06 | 04-01, 04-02 | 상태(Status) 필드 관리 (등록→심사중→확정→보훈청통보완료) | SATISFIED | WoundedStatus 4단계 enum + canTransitionTo() + WoundedListPage 상태 버튼 |
| WOND-07 | 04-01, 04-02 | 보훈청명 코드 테이블 Select | SATISFIED | VeteransOffice 엔티티 + CodeController /veterans-offices + WoundedFormModal showSearch Select |

**요구사항 커버리지: 7/7 SATISFIED (WOND-01 ~ WOND-07)**

---

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| - | - | - | - | 발견된 안티패턴 없음 |

검사 항목:
- TODO/FIXME/PLACEHOLDER 주석: 없음
- `return null` / 빈 구현: 없음 (삭제 API는 ApiResponse.ok(null) 정상 패턴)
- 하드코딩 빈 데이터 (`[]`, `{}` 렌더링): 없음 — 전부 API 훅에서 실제 데이터 수신
- `any` 타입: 없음 — `isAxiosError<{ message: string }>` 타입 가드 사용
- PlaceholderPage 잔재: 없음 — routes/index.tsx에서 WoundedListPage로 교체 완료
- Excel 다운로드 버튼 disabled placeholder: 없음 — 04-03에서 `exportExcel.mutate(searchParams)` 활성화 완료

---

### Human Verification Required

#### 1. 검색 폼 접기/펼치기 UI 동작

**Test:** 브라우저에서 /wounded 접속 후 검색 영역 우상단 "접기/펼치기" 버튼 클릭
**Expected:** searchExpanded state 토글로 검색 필드 Card 영역이 숨겨지고 다시 나타남
**Why human:** DOM 렌더링/CSS transition은 정적 코드 분석으로 확인 불가

#### 2. 보훈청 Select showSearch 필터링

**Test:** 등록 Modal 열고 보훈청명 Select에 텍스트 입력
**Expected:** 입력값에 매칭되는 보훈청/보훈지청 옵션만 표시됨
**Why human:** Ant Design optionFilterProp 실제 런타임 동작 확인 필요

#### 3. Excel 다운로드 역할별 주민번호 마스킹

**Test:** VIEWER/OPERATOR/MANAGER 역할로 각각 로그인 후 Excel 다운로드
**Expected:** VIEWER=전체 마스킹, OPERATOR=뒷자리 마스킹, MANAGER=전체 노출
**Why human:** 실제 DB 데이터 + 역할별 세션으로만 검증 가능

---

### Gaps Summary

갭 없음. 모든 must-have 항목이 검증되었다.

Phase 4의 13개 observable truth 전부 VERIFIED:
- 백엔드: Wounded 엔티티 계층(entity/dto/repository/service/controller) 완전 구현
- 4단계 상태 전이(REGISTERED→UNDER_REVIEW→CONFIRMED→NOTIFIED) + 3종 상이구분 enum 구현
- 군번/주민번호 이중 중복 검증 + SHA-256 해시 + AES-256 암호화
- VeteransOffice 코드 테이블 + /api/codes/veterans-offices 엔드포인트
- Excel: SXSSFWorkbook 15컬럼(보훈청명/병명/상이구분 포함) + 역할별 주민번호 마스킹 + @AuditLog(EXPORT)
- 프론트엔드: TypeScript 타입 + react-query 7훅 + 목록/등록/수정/삭제 페이지 + 라우팅 연결
- WOND-01 ~ WOND-07 요구사항 7개 전부 SATISFIED

---

_Verified: 2026-04-03T03:16:04Z_
_Verifier: Claude (gsd-verifier)_
