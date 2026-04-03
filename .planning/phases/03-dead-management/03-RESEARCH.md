# Phase 3: 사망자 관리 - Research

**Researched:** 2026-04-03
**Domain:** Spring Boot CRUD + QueryDSL 동적 검색 + Ant Design Table/Modal + Apache POI Excel
**Confidence:** HIGH

## Summary

Phase 3은 이 시스템의 첫 번째 비즈니스 도메인 CRUD를 구현하는 핵심 Phase다. Phase 1-2에서 구축한 인프라(BaseAuditEntity, PiiEncryptionConverter, RrnMaskingUtil, AuditLogAspect, SecurityConfig, AuthGuard, QueryDslConfig, ApiResponse)를 활용하여 사망자(TB_DEAD) 도메인의 백엔드 REST API와 프론트엔드 화면을 완성한다.

DB 스키마(V3__create_dead_table.sql)는 이미 Phase 1에서 생성 완료. Entity, Repository, Service, Controller, DTO를 새로 구현하고, 프론트엔드에서는 검색 폼 + 서버사이드 페이징 Table + 등록/수정 Modal + Excel 다운로드를 구현한다. Phase 4(상이자)가 이 Phase의 패턴을 그대로 복사하므로, 재사용 가능한 깔끔한 구조가 중요하다.

**Primary recommendation:** Phase 2의 User CRUD 패턴(record DTO + from() 팩토리, @PreAuthorize, @AuditLog AOP)을 그대로 따르되, QueryDSL 동적 검색과 서버사이드 페이징을 추가한다. 프론트엔드는 @tanstack/react-query의 useQuery/useMutation으로 전환하여 invalidateQueries 자동 갱신 패턴을 적용한다.

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DEAD-01 | 사망자 목록 조회 (군구분/군번/성명/생년월일/계급/소속 복합 검색) | QueryDSL BooleanBuilder 동적 검색 + Spring Data Pageable 서버사이드 페이징 |
| DEAD-02 | 사망자 정보 등록 (군번, 성명, 주민번호, 계급, 소속, 입대일자, 전화번호, 사망구분, 사망코드, 주소, 사망일자) | record DTO + Bean Validation + @AuditLog + PiiEncryptionConverter |
| DEAD-03 | 사망자 정보 수정 | 동일 패턴, PUT API + @AuditLog(action="UPDATE") |
| DEAD-04 | 사망자 논리 삭제 (삭제 사유 필수) | BaseAuditEntity.softDelete() + @SQLRestriction |
| DEAD-05 | Excel 다운로드 (검색 조건 적용, 주민번호 마스킹) | Apache POI SXSSFWorkbook + RrnMaskingUtil |
| DEAD-06 | 상태 필드 관리 (등록->확정->보훈청통보완료) | Java enum DeadStatus + 상태 전이 검증 메서드 |
| DEAD-07 | 중복 등록 방지 (군번 또는 주민번호 기준) | DB UNIQUE 제약조건(군번 이미 존재) + 서비스 레벨 검증 |
</phase_requirements>

## Project Constraints (from CLAUDE.md)

- 코드 주석은 한국어, 식별자/변수명은 영어
- 간결하고 읽기 쉬운 코드 우선, 불필요한 추상화 금지
- TypeScript 사용 시 타입 안전성 유지 (any 남용 금지)
- 파일 800줄, 함수 50줄, 중첩 4단계 한계
- 불변성 패턴 (spread operator로 새 객체)
- 시스템 경계에서 zod/Bean Validation 검증
- record DTO + from() 팩토리 패턴 (Phase 2 결정 사항)
- ApiResponseWrapper<T>로 백엔드 ApiResponse 래퍼와 정합

## Standard Stack

### Core (이미 설치됨 - Phase 1/2)

| Library | Version | Purpose | 비고 |
|---------|---------|---------|------|
| Spring Boot | 3.5.13 | 백엔드 프레임워크 | build.gradle.kts 확인 |
| QueryDSL JPA | 5.1.0 (jakarta) | 동적 검색 쿼리 | QueryDslConfig 빈 등록 완료 |
| Spring Data JPA | (Boot 관리) | Repository + Pageable | 이미 사용 중 |
| React | 18.3.1 | 프론트엔드 | package.json 확인 |
| Ant Design | 6.3.5 | UI 컴포넌트 | Table, Modal, Form, Select, DatePicker |
| @tanstack/react-query | 5.96.1 | 서버 상태 관리 | main.tsx에 QueryClientProvider 설정 완료 |
| axios | 1.14.0 | HTTP 클라이언트 | apiClient 인스턴스 존재 |
| dayjs | 1.11.20 | 날짜 처리 | YYYY.MM.DD 형식 |

### 추가 필요

| Library | Purpose | 설치 위치 |
|---------|---------|----------|
| Apache POI (spring-boot-starter 없음) | Excel 내보내기 | build.gradle.kts에 `org.apache.poi:poi-ooxml` 추가 |

**build.gradle.kts 추가:**
```kotlin
implementation("org.apache.poi:poi-ooxml:5.3.0")
```

## Architecture Patterns

### 백엔드 패키지 구조 (신규 생성)
```
src/main/java/com/navy/casualty/
└── dead/
    ├── controller/
    │   └── DeadController.java          # REST API
    ├── dto/
    │   ├── DeadCreateRequest.java       # 등록 요청 DTO (record)
    │   ├── DeadUpdateRequest.java       # 수정 요청 DTO (record)
    │   ├── DeadSearchRequest.java       # 검색 조건 DTO (record)
    │   ├── DeadResponse.java            # 응답 DTO (record + from())
    │   └── DeadDeleteRequest.java       # 삭제 사유 DTO (record)
    ├── entity/
    │   ├── Dead.java                    # JPA 엔티티
    │   └── DeadStatus.java              # 상태 enum
    ├── repository/
    │   ├── DeadRepository.java          # JpaRepository
    │   └── DeadRepositoryCustom.java    # QueryDSL 인터페이스
    │   └── DeadRepositoryImpl.java      # QueryDSL 구현
    └── service/
        └── DeadService.java             # 비즈니스 로직
        └── DeadExcelService.java        # Excel 내보내기
```

### 프론트엔드 구조 (신규 생성)
```
src/
├── pages/
│   └── dead/
│       ├── DeadListPage.tsx             # 목록 + 검색 폼
│       ├── DeadFormModal.tsx            # 등록/수정 Modal
│       └── DeadDeleteModal.tsx          # 삭제 사유 입력 Modal
├── api/
│   └── dead.ts                          # API 함수 + useQuery/useMutation 훅
└── types/
    └── dead.ts                          # TypeScript 타입 정의
```

### Pattern 1: QueryDSL 동적 검색 + Pageable

**What:** BooleanBuilder로 검색 조건을 동적으로 조합, Pageable로 서버사이드 페이징
**When to use:** 복합 조건 검색이 필요한 모든 목록 API

```java
// DeadRepositoryImpl.java
@RequiredArgsConstructor
public class DeadRepositoryImpl implements DeadRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Dead> search(DeadSearchRequest request, Pageable pageable) {
        QDead dead = QDead.dead;
        BooleanBuilder where = new BooleanBuilder();

        if (request.branchId() != null) {
            where.and(dead.branchId.eq(request.branchId()));
        }
        if (request.serviceNumber() != null && !request.serviceNumber().isBlank()) {
            where.and(dead.serviceNumber.contains(request.serviceNumber()));
        }
        if (request.name() != null && !request.name().isBlank()) {
            where.and(dead.name.contains(request.name()));
        }
        if (request.birthDate() != null) {
            where.and(dead.birthDate.eq(request.birthDate()));
        }
        if (request.rankId() != null) {
            where.and(dead.rankId.eq(request.rankId()));
        }
        if (request.unitId() != null) {
            where.and(dead.unitId.eq(request.unitId()));
        }
        if (request.deathTypeId() != null) {
            where.and(dead.deathTypeId.eq(request.deathTypeId()));
        }

        List<Dead> content = queryFactory
                .selectFrom(dead)
                .where(where)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(dead.id.desc())
                .fetch();

        long total = queryFactory
                .select(dead.count())
                .from(dead)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
```

### Pattern 2: record DTO + from() 팩토리 (Phase 2 결정)

```java
public record DeadResponse(
        Long id,
        String serviceNumber,
        String name,
        String ssnMasked,       // 역할별 마스킹 적용된 주민번호
        LocalDate birthDate,
        String rankName,
        String branchName,
        String unitName,
        LocalDate enlistmentDate,
        String phone,
        String deathTypeName,
        String deathCodeSymbol,
        String address,
        LocalDate deathDate,
        String status,
        LocalDateTime createdAt
) {
    public static DeadResponse from(Dead dead, String maskedSsn,
                                     String rankName, String branchName,
                                     String unitName, String deathTypeName,
                                     String deathCodeSymbol) {
        return new DeadResponse(
                dead.getId(), dead.getServiceNumber(), dead.getName(),
                maskedSsn, dead.getBirthDate(), rankName, branchName,
                unitName, dead.getEnlistmentDate(), dead.getPhone(),
                deathTypeName, deathCodeSymbol, dead.getAddress(),
                dead.getDeathDate(), dead.getStatus().name(),
                dead.getCreatedAt()
        );
    }
}
```

### Pattern 3: @tanstack/react-query 서버 상태 관리

```typescript
// api/dead.ts
export function useDeadList(params: DeadSearchParams) {
  return useQuery({
    queryKey: ['dead', 'list', params],
    queryFn: () => apiClient.get<ApiResponse<PageData<DeadRecord>>>('/dead', { params })
                            .then(res => res.data.data),
  });
}

export function useCreateDead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: DeadCreateForm) => apiClient.post('/dead', data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['dead', 'list'] });
      message.success('등록되었습니다');
    },
  });
}
```

### Pattern 4: 검색 폼 접기/펼치기 (한국 행정 시스템 관례)

```typescript
// DeadListPage.tsx
const [searchExpanded, setSearchExpanded] = useState(true);

<Card
  title="검색 조건"
  extra={
    <Button type="link" onClick={() => setSearchExpanded(!searchExpanded)}>
      {searchExpanded ? '접기' : '펼치기'}
    </Button>
  }
>
  {searchExpanded && (
    <Form form={searchForm} layout="inline" onFinish={handleSearch}>
      {/* 검색 필드 */}
      <Form.Item>
        <Button type="primary" htmlType="submit">조회</Button>
        <Button onClick={handleReset}>초기화</Button>
      </Form.Item>
    </Form>
  )}
</Card>
```

### Pattern 5: 상태 전이 관리

```java
public enum DeadStatus {
    REGISTERED,  // 등록
    CONFIRMED,   // 확정
    NOTIFIED;    // 보훈청통보완료

    // 허용된 상태 전이만 가능
    public boolean canTransitionTo(DeadStatus next) {
        return switch (this) {
            case REGISTERED -> next == CONFIRMED;
            case CONFIRMED -> next == NOTIFIED;
            case NOTIFIED -> false;
        };
    }
}
```

### Pattern 6: SXSSFWorkbook Excel 내보내기

```java
// DeadExcelService.java
@AuditLog(action = "EXPORT", targetTable = "TB_DEAD")
public void exportExcel(DeadSearchRequest request, String userRole,
                        HttpServletResponse response) throws IOException {
    List<Dead> list = deadRepository.searchAll(request); // 페이징 없이 전체

    try (SXSSFWorkbook workbook = new SXSSFWorkbook(100)) { // 100행 메모리 윈도우
        Sheet sheet = workbook.createSheet("사망자 현황");
        // 헤더 행 생성
        // 데이터 행: 주민번호는 RrnMaskingUtil.mask(ssn, userRole) 적용
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=dead_list_" + LocalDate.now() + ".xlsx");
        workbook.write(response.getOutputStream());
    }
}
```

### Anti-Patterns to Avoid

- **Entity를 직접 응답으로 반환:** ssn_encrypted 평문이 노출됨. 반드시 record DTO 통해 마스킹된 값만 반환
- **프론트엔드 클라이언트사이드 페이징:** 전체 데이터를 한 번에 받으면 대량 데이터 시 성능 문제. 반드시 서버사이드 Pageable 사용
- **검색 조건을 @Query 문자열로 하드코딩:** 조건 개수에 따라 N개의 쿼리 메서드 필요. QueryDSL BooleanBuilder 사용
- **주민번호를 프론트엔드에서 복호화:** 복호화는 백엔드에서만 수행, 프론트엔드에는 마스킹된 값만 전달
- **Excel 내보내기 시 XSSFWorkbook 사용:** 대용량 시 OutOfMemoryError. SXSSFWorkbook(스트리밍) 필수

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| 동적 검색 쿼리 | JPQL 문자열 연결 | QueryDSL BooleanBuilder | 타입 안전성, SQL Injection 방지 |
| Excel 생성 | CSV 문자열 | Apache POI SXSSFWorkbook | 대용량 스트리밍, 서식 지원 |
| 주민번호 체크섬 검증 | 자체 알고리즘 | 표준 가중치 알고리즘 (2345678923456 mod 11) | 검증 실수 방지 |
| 서버사이드 페이징 | 수동 offset/limit | Spring Data Pageable + Page<T> | 표준 패턴, total count 자동 |
| 날짜 포맷 파싱 | 정규식 + 수동 파싱 | `@DateTimeFormat(pattern="yyyy.MM.dd")` + dayjs | 로캘 이슈 방지 |

## Common Pitfalls

### Pitfall 1: QueryDSL Q클래스 미생성
**What goes wrong:** `QDead` 클래스가 없어서 컴파일 에러
**Why it happens:** `annotationProcessor("com.querydsl:querydsl-apt:5.1.0:jakarta")`는 이미 build.gradle.kts에 있지만, 엔티티 작성 후 Gradle 빌드를 실행해야 Q클래스가 생성됨
**How to avoid:** Dead 엔티티 작성 후 반드시 `./gradlew compileJava` 실행
**Warning signs:** `cannot find symbol: class QDead`

### Pitfall 2: @SQLRestriction과 논리 삭제된 레코드 중복 검증
**What goes wrong:** 군번 중복 체크 시 @SQLRestriction("deleted_at IS NULL") 때문에 논리 삭제된 레코드가 조회되지 않아, 삭제된 군번으로 재등록 가능
**Why it happens:** 논리 삭제된 레코드도 DB에는 존재하지만 JPA 조회에서 제외됨
**How to avoid:** 중복 검증은 DB UNIQUE 제약조건(service_number)에 의존. 논리 삭제 시 UNIQUE 충돌 방지를 위해 deleted_at을 UNIQUE 조합에 포함하거나, 삭제된 레코드의 군번을 변경하는 전략 필요
**Warning signs:** 삭제된 사망자와 동일 군번으로 재등록 시 DataIntegrityViolationException

### Pitfall 3: PiiEncryptionConverter로 암호화된 주민번호로 검색 불가
**What goes wrong:** 주민번호가 AES-256-GCM으로 암호화 저장되어 WHERE 절에서 검색 불가
**Why it happens:** 암호화된 값은 매번 다른 IV로 생성되어 동일 평문도 다른 암호문 생성
**How to avoid:** 주민번호 검색이 필요하면 birth_date(평문 저장)으로 대체. DEAD-07 중복 검증은 서비스 레벨에서 복호화 후 비교하거나, 주민번호 해시(SHA-256) 별도 컬럼 추가
**Warning signs:** ROADMAP에 "생년월일 별도 컬럼 평문 저장으로 해결" 이미 언급됨

### Pitfall 4: Excel 다운로드 시 인증 세션 문제
**What goes wrong:** axios로 GET 요청 시 파일 다운로드가 안 됨
**Why it happens:** blob 응답 처리 누락, 또는 쿠키 미전송
**How to avoid:** axios에 `responseType: 'blob'` 설정, `window.URL.createObjectURL(blob)`으로 다운로드 트리거, apiClient에 `withCredentials: true` (같은 도메인이면 불필요)
**Warning signs:** 파일이 JSON 텍스트로 다운로드됨

### Pitfall 5: Ant Design Table 페이징과 서버사이드 동기화
**What goes wrong:** 페이지 변경 시 API 재호출이 안 되거나, 검색 후 1페이지로 리셋 안 됨
**Why it happens:** Table의 onChange 이벤트와 useQuery의 queryKey 동기화 미흡
**How to avoid:** page/size를 state로 관리하고 queryKey에 포함. 검색 시 setPage(1) 호출
**Warning signs:** 2페이지에서 검색하면 여전히 2페이지 결과가 표시됨

### Pitfall 6: 주민등록번호 체크섬 검증 로직 오류
**What goes wrong:** 유효한 주민번호가 거부되거나, 무효한 번호가 통과됨
**Why it happens:** 가중치 배열이나 mod 11 계산 실수
**How to avoid:** 표준 알고리즘: 각 자리 * [2,3,4,5,6,7,8,9,2,3,4,5], 합계 mod 11, 11 - 나머지 == 마지막 자리 (10이면 0)
**Warning signs:** 테스트 데이터로 검증 필수

## Code Examples

### 주민등록번호 체크섬 검증 (프론트엔드)

```typescript
// utils/rrnValidation.ts
export function validateRrn(rrn: string): boolean {
  // "YYMMDD-NNNNNNN" 형식 (하이픈 포함 14자, 숫자 13자리)
  const digits = rrn.replace('-', '');
  if (digits.length !== 13 || !/^\d{13}$/.test(digits)) return false;

  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
  let sum = 0;
  for (let i = 0; i < 12; i++) {
    sum += Number(digits[i]) * weights[i];
  }
  const checkDigit = (11 - (sum % 11)) % 10;
  return checkDigit === Number(digits[12]);
}
```

### 백엔드 Dead 엔티티

```java
@Entity
@Table(name = "TB_DEAD")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Dead extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_number", nullable = false, unique = true, length = 20)
    private String serviceNumber;

    @Column(nullable = false, length = 50)
    private String name;

    @Convert(converter = PiiEncryptionConverter.class)
    @Column(name = "ssn_encrypted", nullable = false)
    private String ssnEncrypted;  // 평문 주민번호 -> DB 자동 암호화

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "rank_id")
    private Long rankId;

    @Column(name = "branch_id")
    private Long branchId;

    @Column(name = "unit_id")
    private Long unitId;

    @Column(name = "enlistment_date")
    private LocalDate enlistmentDate;

    @Column(length = 20)
    private String phone;

    @Column(name = "death_type_id")
    private Long deathTypeId;

    @Column(name = "death_code_id")
    private Long deathCodeId;

    @Column
    private String address;

    @Column(name = "death_date", nullable = false)
    private LocalDate deathDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DeadStatus status = DeadStatus.REGISTERED;

    public void updateStatus(DeadStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                    this.status + "에서 " + newStatus + "로 전환할 수 없습니다");
        }
        this.status = newStatus;
    }
}
```

### 백엔드 Controller 예시

```java
@RestController
@RequestMapping("/api/dead")
@RequiredArgsConstructor
public class DeadController {

    private final DeadService deadService;
    private final DeadExcelService deadExcelService;

    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<DeadResponse>>> list(
            DeadSearchRequest request, Pageable pageable) {
        Page<DeadResponse> page = deadService.search(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(page));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<DeadResponse>> create(
            @Valid @RequestBody DeadCreateRequest request) {
        DeadResponse response = deadService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<DeadResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DeadUpdateRequest request) {
        DeadResponse response = deadService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @Valid @RequestBody DeadDeleteRequest request) {
        deadService.softDelete(id, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam DeadStatus status) {
        deadService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @GetMapping("/excel")
    @PreAuthorize("hasRole('VIEWER')")
    public void exportExcel(DeadSearchRequest request,
                            HttpServletResponse response) throws IOException {
        deadExcelService.exportExcel(request, response);
    }
}
```

### 프론트엔드 API 레이어 예시

```typescript
// api/dead.ts
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from './client';

export const deadKeys = {
  all: ['dead'] as const,
  lists: () => [...deadKeys.all, 'list'] as const,
  list: (params: DeadSearchParams) => [...deadKeys.lists(), params] as const,
};

export function useDeadList(params: DeadSearchParams) {
  return useQuery({
    queryKey: deadKeys.list(params),
    queryFn: async () => {
      const res = await apiClient.get('/dead', { params });
      return res.data.data;
    },
  });
}

export function useCreateDead() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: DeadCreateForm) =>
      apiClient.post('/dead', data).then(r => r.data),
    onSuccess: () => qc.invalidateQueries({ queryKey: deadKeys.lists() }),
  });
}

export function useExportDeadExcel() {
  return useMutation({
    mutationFn: async (params: DeadSearchParams) => {
      const res = await apiClient.get('/dead/excel', {
        params,
        responseType: 'blob',
      });
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.download = `dead_list_${new Date().toISOString().slice(0,10)}.xlsx`;
      link.click();
      window.URL.revokeObjectURL(url);
    },
  });
}
```

## State of the Art

| Old Approach | Current Approach | Impact |
|--------------|------------------|--------|
| Ant Design v4 Table | Ant Design v6 Table | 현재 프로젝트는 antd 6.3.5 사용. v6 API 확인 필요 |
| useState + useEffect fetch | @tanstack/react-query | 이미 설치됨. 캐시 무효화, 로딩 상태 자동 관리 |
| XSSFWorkbook | SXSSFWorkbook | 스트리밍 방식으로 메모리 효율적 |

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Backend Framework | JUnit 5 + Spring Boot Test (build.gradle.kts) |
| Backend Config file | 없음 (기본 설정) |
| Backend Quick run | `./gradlew test --tests "com.navy.casualty.dead.*"` |
| Backend Full suite | `./gradlew test` |
| Frontend Framework | Vitest 4.1.2 + Testing Library |
| Frontend Config file | vitest.config.ts |
| Frontend Quick run | `npm run test:run -- --reporter=verbose` |
| Frontend Full suite | `npm run test:run` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DEAD-01 | 복합 조건 검색 + 페이징 | unit + integration | `./gradlew test --tests "*.dead.DeadRepositoryTest"` | Wave 0 |
| DEAD-02 | 사망자 등록 API | integration | `./gradlew test --tests "*.dead.DeadControllerTest"` | Wave 0 |
| DEAD-03 | 사망자 수정 API | integration | `./gradlew test --tests "*.dead.DeadControllerTest"` | Wave 0 |
| DEAD-04 | 논리 삭제 (사유 필수) | unit + integration | `./gradlew test --tests "*.dead.DeadServiceTest"` | Wave 0 |
| DEAD-05 | Excel 내보내기 | integration | `./gradlew test --tests "*.dead.DeadExcelServiceTest"` | Wave 0 |
| DEAD-06 | 상태 전이 검증 | unit | `./gradlew test --tests "*.dead.DeadStatusTest"` | Wave 0 |
| DEAD-07 | 중복 등록 방지 | integration | `./gradlew test --tests "*.dead.DeadServiceTest"` | Wave 0 |

### Sampling Rate
- **Per task commit:** `./gradlew test --tests "com.navy.casualty.dead.*" -x processTestAot`
- **Per wave merge:** `./gradlew test`
- **Phase gate:** Full suite green

### Wave 0 Gaps
- [ ] `DeadRepositoryTest.java` -- QueryDSL 동적 검색 테스트
- [ ] `DeadControllerTest.java` -- REST API 통합 테스트
- [ ] `DeadServiceTest.java` -- 비즈니스 로직 단위 테스트
- [ ] `DeadExcelServiceTest.java` -- Excel 내보내기 테스트
- [ ] `DeadStatusTest.java` -- 상태 전이 enum 테스트
- [ ] `DeadListPage.test.tsx` -- 프론트엔드 목록 화면 테스트
- [ ] `rrnValidation.test.ts` -- 주민번호 체크섬 검증 테스트

## Open Questions

1. **TB_DEAD의 군번 UNIQUE와 논리 삭제 충돌**
   - What we know: TB_DEAD.service_number는 UNIQUE, 논리 삭제된 레코드는 deleted_at IS NOT NULL
   - What's unclear: 동일 군번이 삭제 후 재등록 가능해야 하는지
   - Recommendation: 현재 DB 스키마상 재등록 불가(UNIQUE 유지). 필요 시 Flyway 마이그레이션으로 UNIQUE 제약을 (service_number, deleted_at) 복합으로 변경

2. **코드 테이블 엔티티 존재 여부**
   - What we know: TB_RANK_CODE, TB_BRANCH_CODE, TB_DEATH_TYPE, TB_DEATH_CODE, TB_UNIT_CODE 테이블은 V2에서 생성됨
   - What's unclear: 이 코드 테이블들의 JPA Entity가 아직 없음
   - Recommendation: Phase 3에서 코드 테이블 Entity + Repository + 코드 조회 API를 함께 생성 (프론트엔드 Select 드롭다운용)

3. **심사 이력 연계 (사망구분 자동 갱신)**
   - What we know: ROADMAP에 "전공사상심사 결과로 사망구분 자동 갱신" 명시
   - What's unclear: Phase 5(전공사상심사)가 아직 없으므로 연계 인터페이스만 준비
   - Recommendation: DeadService에 `updateDeathTypeFromReview(Long deadId, Long deathTypeId)` 메서드만 선언, Phase 5에서 호출

## Sources

### Primary (HIGH confidence)
- 프로젝트 소스코드 직접 분석 (build.gradle.kts, 기존 Entity/Controller/Service 패턴)
- DB 마이그레이션 스크립트 (V2, V3, V9)
- REQUIREMENTS.md DEAD-01~07 요구사항

### Secondary (MEDIUM confidence)
- QueryDSL 5.x BooleanBuilder 패턴 (training data)
- Apache POI SXSSFWorkbook API (training data)
- 주민등록번호 체크섬 알고리즘 (공개 표준)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - 모든 라이브러리가 프로젝트에 이미 존재하거나 Apache POI만 추가
- Architecture: HIGH - Phase 2 패턴을 그대로 확장, 프로젝트 코드 직접 확인
- Pitfalls: HIGH - DB 스키마와 기존 코드 분석 기반
- QueryDSL 동적 검색: MEDIUM - 패턴은 표준이나 Ant Design v6 Table API는 확인 필요

**Research date:** 2026-04-03
**Valid until:** 2026-05-03 (안정적 스택, 내부 프로젝트)
