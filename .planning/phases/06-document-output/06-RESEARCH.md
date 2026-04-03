# Phase 6: 공식 문서 출력 - Research

**Researched:** 2026-04-03
**Domain:** JasperReports 문서 생성 + React 인쇄 미리보기
**Confidence:** MEDIUM

## Summary

JasperReports를 Spring Boot 3.x에 통합하여 공식 문서 6종을 PDF로 생성하고, 프론트엔드에서 인쇄 미리보기를 제공하는 Phase이다. 핵심 결정 사항은 (1) JasperReports 버전 선택, (2) PDF vs HTML 렌더링 전략, (3) 한글 폰트 처리, (4) TB_DOCUMENT_ISSUE 발급 이력 기록이다.

에어갭 군 내부망 환경이므로 외부 CDN 폰트 로딩이 불가하다. 폰트 파일(.ttf)을 프로젝트에 번들링하고 JasperReports 폰트 확장(font extension)으로 등록해야 한다. TB_DOCUMENT_ISSUE 테이블은 V7 마이그레이션에서 이미 생성되어 있으므로 추가 마이그레이션은 불필요하다.

해군 공식 양식 원본이 없으므로 표준 군 공문서 양식 기반 프로토타입 .jrxml 템플릿을 구현하고, 실제 양식 입수 시 .jrxml 파일만 교체하는 구조로 설계한다.

**Primary recommendation:** JasperReports 6.21.3 (LGPL, iText 무의존) + PDF byte[] 응답 + react-to-print 3.3.0 으로 구현한다. HTML 렌더링은 폰트/레이아웃 불일치 위험이 높아 PDF 직접 다운로드/브라우저 내장 뷰어 방식이 에어갭 환경에 최적이다.

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| DOCU-01 | 국가유공자 요건 해당사실 확인서(사망자용) 출력 | JasperReports .jrxml 템플릿 + Dead 엔티티 데이터 조회 |
| DOCU-02 | 국가유공자 요건 해당사실 확인서(상이자용) 출력 | JasperReports .jrxml 템플릿 + Wounded 엔티티 데이터 조회 |
| DOCU-03 | 전공사상심사결과서 출력 | JasperReports .jrxml 템플릿 + Review 엔티티 데이터 조회 |
| DOCU-04 | 순직/사망확인서 출력 | JasperReports .jrxml 템플릿 + Dead 엔티티 데이터 조회 |
| DOCU-05 | 사망자 현황 보고서 출력 | JasperReports .jrxml 리스트 템플릿 + Dead 전체 검색 |
| DOCU-06 | 상이자 현황 보고서 출력 | JasperReports .jrxml 리스트 템플릿 + Wounded 전체 검색 |
| DOCU-07 | 전사망자 확인증 발급대장 출력 | JasperReports .jrxml 리스트 템플릿 + DocumentIssue 이력 조회 |
| DOCU-08 | 발급 목적 필수 입력 + 발급 이력 자동 기록 | TB_DOCUMENT_ISSUE 엔티티 + 발급 목적 Modal + @AuditLog |
| DOCU-09 | 인쇄 미리보기 | react-to-print 3.3.0 + iframe PDF 뷰어 + A4 CSS |
</phase_requirements>

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| JasperReports | 6.21.3 | 문서 템플릿 기반 PDF 생성 | LGPL 라이선스, iText 무의존, Spring Boot 3 호환 |
| react-to-print | 3.3.0 | 브라우저 인쇄 미리보기 | React 18 호환, 경량, 유지보수 활발 |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| NanumGothic TTF | - | 한글 폰트 번들 | .jrxml 템플릿 + PDF 한글 렌더링 |
| jasperreports-fonts (custom) | - | JasperReports 폰트 확장 | 에어갭 환경 폰트 임베딩 |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| JasperReports 6.21.3 | JasperReports 7.0.3 | 7.x는 모듈 구조 변경(jasperreports-pdf 별도), Spring Boot 3 호환성 검증 부족. 6.21.3이 안정적 |
| JasperReports | Apache FOP (XSL-FO) | FOP는 범용적이나 양식 기반 문서에 JasperReports가 더 적합 |
| PDF 직접 다운로드 | HTML 렌더링 후 인쇄 | HTML은 폰트/레이아웃 정확도가 PDF 대비 낮음. 공식 문서에 부적합 |

**Installation:**

백엔드 (build.gradle.kts):
```kotlin
implementation("net.sf.jasperreports:jasperreports:6.21.3")
```

프론트엔드:
```bash
npm install react-to-print@3.3.0
```

**Version verification:**
- JasperReports 6.21.3: Maven Central 2024-04-17 릴리스 (MEDIUM confidence - mvnrepository 확인)
- react-to-print 3.3.0: npm registry 확인 완료 (HIGH confidence)

## Architecture Patterns

### Recommended Project Structure
```
backend/src/main/
├── java/com/navy/casualty/document/
│   ├── controller/
│   │   └── DocumentController.java          # REST API (/api/documents)
│   ├── service/
│   │   ├── DocumentGenerationService.java   # JasperReports 렌더링
│   │   └── DocumentIssueService.java        # 발급 이력 CRUD
│   ├── entity/
│   │   └── DocumentIssue.java               # TB_DOCUMENT_ISSUE 엔티티
│   ├── dto/
│   │   ├── DocumentIssueRequest.java        # 발급 요청 (목적 포함)
│   │   ├── DocumentIssueResponse.java       # 발급 이력 응답
│   │   └── DocumentIssueSearchRequest.java  # 이력 검색
│   ├── repository/
│   │   ├── DocumentIssueRepository.java
│   │   ├── DocumentIssueRepositoryCustom.java
│   │   └── DocumentIssueRepositoryImpl.java
│   └── enums/
│       └── DocumentType.java                # 문서 유형 enum (6종)
├── resources/
│   ├── reports/
│   │   ├── dead_certificate.jrxml           # DOCU-01 사망자 확인서
│   │   ├── wounded_certificate.jrxml        # DOCU-02 상이자 확인서
│   │   ├── review_result.jrxml              # DOCU-03 심사결과서
│   │   ├── death_confirmation.jrxml         # DOCU-04 순직/사망확인서
│   │   ├── dead_status_report.jrxml         # DOCU-05 사망자 현황 보고서
│   │   ├── wounded_status_report.jrxml      # DOCU-06 상이자 현황 보고서
│   │   └── issue_ledger.jrxml               # DOCU-07 발급대장
│   └── fonts/
│       ├── NanumGothic.ttf
│       ├── NanumGothicBold.ttf
│       └── jasperreports_extension.properties
│
frontend/src/
├── api/
│   └── document.ts                          # 문서 API 훅
├── types/
│   └── document.ts                          # 문서 타입 정의
├── pages/document/
│   ├── DocumentListPage.tsx                 # 문서 출력 메인 페이지
│   ├── DocumentIssuePurposeModal.tsx        # 발급 목적 입력 팝업
│   ├── DocumentPreviewModal.tsx             # PDF 미리보기 Modal
│   └── DocumentIssueHistoryPage.tsx         # 발급 이력 조회
└── components/print/
    └── PrintStyles.css                      # A4 인쇄 CSS
```

### Pattern 1: PDF byte[] 응답 컨트롤러
**What:** JasperReports가 생성한 PDF를 byte[]로 반환하는 REST 엔드포인트
**When to use:** 모든 문서 출력 요청 시
**Example:**
```java
// DocumentController.java
@PostMapping("/{documentType}/generate")
@PreAuthorize("hasRole('OPERATOR')")
public ResponseEntity<byte[]> generateDocument(
        @PathVariable DocumentType documentType,
        @RequestParam Long targetId,
        @Valid @RequestBody DocumentIssueRequest request) {

    // 1. 발급 이력 기록 (DOCU-08)
    documentIssueService.recordIssue(documentType, targetId, request.purpose());

    // 2. PDF 생성
    byte[] pdfBytes = documentGenerationService.generate(documentType, targetId);

    // 3. PDF 바이트 반환
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_PDF);
    headers.setContentDispositionFormData("inline",
        documentType.getFileName() + ".pdf");
    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
}
```

### Pattern 2: JasperReports 서비스 구현
**What:** .jrxml 컴파일 + 데이터 매핑 + PDF 렌더링 서비스
**When to use:** 문서 생성 로직
**Example:**
```java
// DocumentGenerationService.java
@Service
@RequiredArgsConstructor
public class DocumentGenerationService {

    private final DeadRepository deadRepository;
    private final WoundedRepository woundedRepository;
    private final ReviewRepository reviewRepository;

    public byte[] generate(DocumentType type, Long targetId) {
        try {
            // 1. .jrxml 파일 로드 + 컴파일 (캐싱 권장)
            InputStream jrxmlStream = getClass().getResourceAsStream(
                "/reports/" + type.getTemplateName() + ".jrxml");
            JasperReport report = JasperCompileManager.compileReport(jrxmlStream);

            // 2. 파라미터 + 데이터 소스 구성
            Map<String, Object> params = buildParameters(type, targetId);

            // 3. 보고서 채우기
            JasperPrint print = JasperFillManager.fillReport(
                report, params, new JREmptyDataSource());

            // 4. PDF 내보내기
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) {
            throw new IllegalStateException("문서 생성 실패: " + type, e);
        }
    }
}
```

### Pattern 3: 프론트엔드 PDF 미리보기 + 인쇄
**What:** PDF blob을 iframe으로 표시하고 react-to-print으로 인쇄
**When to use:** DOCU-09 인쇄 미리보기
**Example:**
```typescript
// DocumentPreviewModal.tsx
import { useReactToPrint } from 'react-to-print';

function DocumentPreviewModal({ pdfBlob, onClose }: Props) {
  const iframeRef = useRef<HTMLIFrameElement>(null);
  const pdfUrl = useMemo(() =>
    pdfBlob ? URL.createObjectURL(pdfBlob) : '', [pdfBlob]);

  const handlePrint = useReactToPrint({
    contentRef: iframeRef,
  });

  return (
    <Modal open={!!pdfBlob} onCancel={onClose} width={800}
      footer={[
        <Button key="print" type="primary" onClick={handlePrint}>인쇄</Button>,
        <Button key="download" onClick={() => downloadBlob(pdfBlob)}>다운로드</Button>,
      ]}>
      <iframe ref={iframeRef} src={pdfUrl}
        style={{ width: '100%', height: '80vh', border: 'none' }} />
    </Modal>
  );
}
```

### Pattern 4: 발급 목적 입력 팝업 (DOCU-08)
**What:** 문서 출력 전 발급 목적을 필수 입력받는 Modal
**When to use:** 모든 문서 출력 버튼 클릭 시
**Example:**
```typescript
// DocumentIssuePurposeModal.tsx
function DocumentIssuePurposeModal({ onConfirm, onCancel }: Props) {
  const [form] = Form.useForm();

  return (
    <Modal title="발급 목적 입력" open onOk={() => form.validateFields().then(onConfirm)}
      onCancel={onCancel}>
      <Form form={form}>
        <Form.Item name="purpose" label="발급 목적"
          rules={[{ required: true, message: '발급 목적을 입력하세요' },
                  { max: 500, message: '500자 이내로 입력하세요' }]}>
          <Input.TextArea rows={3} />
        </Form.Item>
      </Form>
    </Modal>
  );
}
```

### Anti-Patterns to Avoid
- **매번 .jrxml 컴파일:** .jasper 컴파일 결과를 캐싱하지 않으면 요청마다 컴파일 비용 발생. `@PostConstruct`에서 미리 컴파일하거나 Map 캐시 사용
- **HTML 렌더링으로 공식 문서 출력:** 브라우저/OS별 렌더링 차이로 레이아웃 깨짐. 공식 문서는 반드시 PDF 사용
- **iText 의존성 추가:** 6.21.3은 iText 없이 동작. iText 추가 시 AGPL 라이선스 위반 위험
- **폰트를 시스템 폰트에 의존:** 에어갭 환경에서 OS 폰트 유무 보장 불가. 반드시 프로젝트 내 번들링

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| PDF 생성 | 직접 PDF 바이트 조합 | JasperReports | 페이지 분할, 폰트 임베딩, 테이블 레이아웃 등 복잡도 극심 |
| 인쇄 미리보기 | window.print() 직접 호출 | react-to-print | CSS 스타일 복사, 크로스 브라우저 호환 처리 |
| A4 페이지 레이아웃 | CSS @page 직접 구현 | JasperReports .jrxml | 브라우저별 @page 지원 불일치 |
| 한글 폰트 PDF 임베딩 | 직접 TrueType 파싱 | JasperReports 폰트 확장 | Identity-H 인코딩 + 서브셋 임베딩 자동 처리 |

**Key insight:** 공식 문서 출력은 "정확한 레이아웃 재현"이 핵심. HTML/CSS 기반 렌더링은 브라우저별 차이로 공식 문서 품질 달성 불가. JasperReports PDF가 유일한 현실적 해법.

## Common Pitfalls

### Pitfall 1: 한글 PDF 깨짐 (글자가 사각형으로 표시)
**What goes wrong:** PDF 열면 한글이 사각형(tofu)이나 ?로 표시됨
**Why it happens:** JasperReports 기본 폰트(SansSerif 등)에 한글 글리프 없음. pdfEncoding, pdfEmbedded 미설정
**How to avoid:**
1. NanumGothic.ttf를 `resources/fonts/`에 번들
2. `jasperreports_extension.properties` 작성:
```properties
net.sf.jasperreports.extension.registry.factory.fonts=net.sf.jasperreports.engine.fonts.SimpleFontExtensionRegistryFactory
net.sf.jasperreports.extension.simple.font.families.nanum=fonts/fonts.xml
```
3. `fonts.xml`에 폰트 매핑:
```xml
<fontFamilies>
  <fontFamily name="NanumGothic">
    <normal>fonts/NanumGothic.ttf</normal>
    <bold>fonts/NanumGothicBold.ttf</bold>
    <pdfEncoding>Identity-H</pdfEncoding>
    <pdfEmbedded>true</pdfEmbedded>
  </fontFamily>
</fontFamilies>
```
4. .jrxml에서 `fontName="NanumGothic"` 사용
**Warning signs:** 로컬에서 한글 보이지만 Docker/서버에서 깨짐 (로컬 OS 폰트 참조)

### Pitfall 2: .jrxml 컴파일 실패 (JRException)
**What goes wrong:** 애플리케이션 시작 또는 첫 문서 생성 시 JRException 발생
**Why it happens:** .jrxml XML 구문 오류, 필드명 불일치, JasperReports 버전과 .jrxml 스키마 불일치
**How to avoid:** .jrxml 파일은 Jaspersoft Studio로 설계하되, 프로젝트 내에서 단위 테스트로 컴파일 검증
**Warning signs:** `net.sf.jasperreports.engine.JRException: Error compiling report design`

### Pitfall 3: 대용량 리스트 보고서 OutOfMemoryError
**What goes wrong:** DOCU-05/06/07 리스트 보고서에서 전체 데이터를 메모리에 로드하면 OOM
**Why it happens:** JasperReports가 JRBeanCollectionDataSource 전체를 메모리에 보관
**How to avoid:** 리스트 보고서에 페이징 적용 (예: 최근 1년, 또는 검색 조건 필수). 현실적으로 군 사상자 데이터는 수천건 이하이므로 OOM 가능성 낮지만 방어적 설계 권장
**Warning signs:** 보고서 생성 시 응답 지연 > 10초

### Pitfall 4: Content-Disposition 한글 파일명 깨짐
**What goes wrong:** 다운로드 파일명의 한글이 깨져서 표시됨
**Why it happens:** HTTP 헤더의 Content-Disposition에서 한글 인코딩 미처리
**How to avoid:** `URLEncoder.encode(fileName, StandardCharsets.UTF_8)` + `filename*=UTF-8''` 헤더 사용
**Warning signs:** 파일명이 `%EC%82%AC%EB%A7%9D` 같은 인코딩 문자로 표시

### Pitfall 5: react-to-print과 iframe PDF 뷰어 충돌
**What goes wrong:** react-to-print이 iframe 내부 PDF를 인쇄하지 못함
**Why it happens:** 브라우저 보안 정책으로 cross-origin iframe 접근 제한, PDF 플러그인 차이
**How to avoid:** PDF blob URL은 same-origin이므로 기본적으로 동작. 단, 인쇄 버튼은 iframe의 `contentWindow.print()` 직접 호출이 더 안정적. react-to-print은 HTML 컨텐츠 인쇄용으로만 활용
**Warning signs:** 인쇄 미리보기에서 빈 페이지 출력

## Code Examples

### JasperReports 폰트 확장 설정
```properties
# resources/jasperreports_extension.properties
net.sf.jasperreports.extension.registry.factory.fonts=net.sf.jasperreports.engine.fonts.SimpleFontExtensionRegistryFactory
net.sf.jasperreports.extension.simple.font.families.nanum=fonts/fonts.xml
```

### 폰트 XML 설정
```xml
<!-- resources/fonts/fonts.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<fontFamilies>
  <fontFamily name="NanumGothic">
    <normal>fonts/NanumGothic.ttf</normal>
    <bold>fonts/NanumGothicBold.ttf</bold>
    <pdfEncoding>Identity-H</pdfEncoding>
    <pdfEmbedded>true</pdfEmbedded>
  </fontFamily>
</fontFamilies>
```

### 프로토타입 .jrxml 템플릿 구조 (A4)
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jasperReport xmlns="http://jasperreports.sourceforge.net/jasperreports"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://jasperreports.sourceforge.net/jasperreports
    http://jasperreports.sourceforge.net/xsd/jasperreport.xsd"
  name="dead_certificate"
  pageWidth="595" pageHeight="842"
  leftMargin="50" rightMargin="50" topMargin="50" bottomMargin="50"
  columnWidth="495">

  <parameter name="name" class="java.lang.String"/>
  <parameter name="serviceNumber" class="java.lang.String"/>
  <parameter name="rankName" class="java.lang.String"/>
  <parameter name="birthDate" class="java.lang.String"/>
  <parameter name="deathDate" class="java.lang.String"/>
  <parameter name="deathTypeName" class="java.lang.String"/>
  <parameter name="unitName" class="java.lang.String"/>
  <parameter name="issueDate" class="java.lang.String"/>

  <title>
    <band height="100">
      <staticText>
        <reportElement x="0" y="30" width="495" height="40"/>
        <textElement textAlignment="Center">
          <font fontName="NanumGothic" size="18" isBold="true"/>
        </textElement>
        <text><![CDATA[국가유공자 요건 해당사실 확인서]]></text>
      </staticText>
    </band>
  </title>

  <detail>
    <band height="400">
      <!-- 인적사항 테이블 -->
      <staticText>
        <reportElement x="0" y="10" width="100" height="25"/>
        <textElement><font fontName="NanumGothic" size="11"/></textElement>
        <text><![CDATA[성    명]]></text>
      </staticText>
      <textField>
        <reportElement x="110" y="10" width="200" height="25"/>
        <textElement><font fontName="NanumGothic" size="11"/></textElement>
        <textFieldExpression><![CDATA[$P{name}]]></textFieldExpression>
      </textField>
    </band>
  </detail>
</jasperReport>
```

### DocumentType Enum
```java
public enum DocumentType {
    DEAD_CERTIFICATE("dead_certificate", "국가유공자_확인서_사망자", "TB_DEAD"),
    WOUNDED_CERTIFICATE("wounded_certificate", "국가유공자_확인서_상이자", "TB_WOUNDED"),
    REVIEW_RESULT("review_result", "전공사상심사결과서", "TB_REVIEW"),
    DEATH_CONFIRMATION("death_confirmation", "순직사망확인서", "TB_DEAD"),
    DEAD_STATUS_REPORT("dead_status_report", "사망자현황보고서", "TB_DEAD"),
    WOUNDED_STATUS_REPORT("wounded_status_report", "상이자현황보고서", "TB_WOUNDED"),
    ISSUE_LEDGER("issue_ledger", "발급대장", "TB_DOCUMENT_ISSUE");

    private final String templateName;
    private final String fileName;
    private final String targetTable;

    // constructor, getters
}
```

### DocumentIssue 엔티티 (V7 테이블 매핑)
```java
@Entity
@Table(name = "TB_DOCUMENT_ISSUE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class DocumentIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Column(name = "target_table", length = 50)
    private String targetTable;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "issue_purpose", nullable = false, length = 500)
    private String issuePurpose;

    @Column(name = "issued_by", nullable = false, length = 50)
    private String issuedBy;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;
}
```

### 프론트엔드 문서 API 훅 (기존 패턴 준수)
```typescript
// api/document.ts
export const documentKeys = {
  all: ['document'] as const,
  issues: () => [...documentKeys.all, 'issue'] as const,
  issueList: (params: DocumentIssueSearchParams) =>
    [...documentKeys.issues(), params] as const,
};

export function useGenerateDocument() {
  return useMutation({
    mutationFn: async ({
      documentType, targetId, purpose
    }: GenerateDocumentParams) => {
      const res = await apiClient.post(
        `/documents/${documentType}/generate`,
        { purpose },
        { params: { targetId }, responseType: 'blob' }
      );
      return new Blob([res.data], { type: 'application/pdf' });
    },
    onError: (error: unknown) => {
      message.error(getErrorMessage(error, '문서 생성에 실패했습니다'));
    },
  });
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| JasperReports + iText 2.1.7 | JasperReports 6.20.1+ iText 무의존 | 2023-03 | LGPL 순수 유지 가능 |
| JasperReports 6.x 단일 JAR | JasperReports 7.x 모듈 분리 (jasperreports-pdf 별도) | 2024-06 | 7.x는 신규 프로젝트용. 6.21.3이 안정적 |
| window.print() 직접 호출 | react-to-print 3.x | 2024+ | CSS 복사, 콜백 지원 등 안정화 |

**Deprecated/outdated:**
- iText 2.1.7 내장: 6.20.1부터 제거됨. 별도 추가 불필요
- JasperReports 5.x: Jakarta namespace 미지원. Spring Boot 3과 호환 불가

## Open Questions

1. **NanumGothic TTF 파일 라이선스 및 번들 방법**
   - What we know: NanumGothic은 OFL(Open Font License)로 자유 사용 가능
   - What's unclear: 군 내부망에서 폰트 파일 배포 승인 절차
   - Recommendation: NanumGothic OFL 라이선스 확인 후 `resources/fonts/`에 직접 포함. 배포 승인은 별도 행정 절차

2. **JasperReports 6.21.3의 Spring Boot 3.5.x 호환성**
   - What we know: 6.21.3은 javax가 아닌 jakarta namespace를 지원하는 마지막 6.x 버전으로 추정
   - What's unclear: 공식 호환성 매트릭스가 없음
   - Recommendation: 의존성 추가 후 컴파일 테스트로 확인. 실패 시 7.0.3으로 전환 (fallback)

3. **리스트 보고서(DOCU-05/06/07)의 검색 조건 범위**
   - What we know: 전체 데이터를 보고서로 출력 가능해야 함
   - What's unclear: 기간 필터 등 검색 조건 적용 범위
   - Recommendation: 기존 검색 폼과 동일 조건을 보고서 파라미터로 전달

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 5 (Spring Boot Test) + Vitest 4.1.2 |
| Config file | backend: build.gradle.kts / frontend: vitest config in vite.config.ts |
| Quick run command | `cd backend && ./gradlew test --tests "*.document.*"` |
| Full suite command | `cd backend && ./gradlew test && cd ../frontend && npm run test:run` |

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| DOCU-01~04 | 개별 문서 PDF 생성 | unit | `./gradlew test --tests "*DocumentGenerationServiceTest*"` | Wave 0 |
| DOCU-05~06 | 리스트 보고서 PDF 생성 | unit | `./gradlew test --tests "*DocumentGenerationServiceTest*"` | Wave 0 |
| DOCU-07 | 발급대장 PDF 생성 | unit | `./gradlew test --tests "*DocumentGenerationServiceTest*"` | Wave 0 |
| DOCU-08 | 발급 이력 기록 | unit | `./gradlew test --tests "*DocumentIssueServiceTest*"` | Wave 0 |
| DOCU-09 | 인쇄 미리보기 | manual-only | 브라우저 수동 테스트 (PDF iframe 렌더링) | - |

### Sampling Rate
- **Per task commit:** `./gradlew test --tests "*.document.*"`
- **Per wave merge:** `./gradlew test && cd ../frontend && npm run test:run`
- **Phase gate:** Full suite green before `/gsd:verify-work`

### Wave 0 Gaps
- [ ] `DocumentGenerationServiceTest.java` -- .jrxml 컴파일 + PDF 바이트 생성 검증
- [ ] `DocumentIssueServiceTest.java` -- 발급 이력 CRUD 검증
- [ ] `DocumentControllerTest.java` -- REST API 통합 테스트
- [ ] 테스트용 간소화 .jrxml (단순 텍스트만 포함)

## Sources

### Primary (HIGH confidence)
- [mvnrepository - JasperReports 6.21.3](https://mvnrepository.com/artifact/net.sf.jasperreports/jasperreports/6.21.3) - 버전/릴리스 날짜 확인
- npm registry - react-to-print 3.3.0 (`npm view` 명령 확인)
- 프로젝트 소스 코드 직접 분석 (Dead/Wounded/Review 엔티티, API 패턴, V7 마이그레이션)

### Secondary (MEDIUM confidence)
- [Jaspersoft Community - Korean CJK Font](https://community.jaspersoft.com/questions/540599/jrxml-locales-and-related-fonts-cjk) - 한글 폰트 설정 방법
- [Jaspersoft Community - iText LGPL](https://community.jaspersoft.com/knowledgebase/faq/issue-concerning-itext-licensing-tibco-jaspersoft/) - 6.20.1+ iText 무의존 확인
- [react-to-print GitHub](https://github.com/MatthewHerbst/react-to-print) - API 사용법

### Tertiary (LOW confidence)
- JasperReports 6.21.3 + Spring Boot 3.5.x 호환성 (공식 매트릭스 미확인, 실제 테스트 필요)

## Metadata

**Confidence breakdown:**
- Standard stack: MEDIUM - JasperReports 6.21.3의 Spring Boot 3.5 호환성 공식 미검증. react-to-print은 HIGH
- Architecture: HIGH - 기존 프로젝트 패턴(Controller/Service/Repository, API 훅)을 그대로 확장
- Pitfalls: HIGH - 한글 폰트 PDF 깨짐은 JasperReports + CJK의 대표적 문제. 다수 소스에서 확인

**Research date:** 2026-04-03
**Valid until:** 2026-05-03 (JasperReports는 안정적 릴리스 주기)
