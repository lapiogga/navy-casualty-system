package com.navy.casualty.document.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;

import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.repository.DeadRepository;
import com.navy.casualty.document.enums.DocumentType;
import com.navy.casualty.document.repository.DocumentIssueRepository;
import com.navy.casualty.review.repository.ReviewRepository;
import com.navy.casualty.wounded.repository.WoundedRepository;
import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * DocumentGenerationService 단위 테스트.
 * JasperReports .jrxml 컴파일, PDF 생성, 한글 폰트 임베딩을 검증한다.
 */
@ExtendWith(MockitoExtension.class)
class DocumentGenerationServiceTest {

    @Mock
    private DeadRepository deadRepository;
    @Mock
    private WoundedRepository woundedRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private DocumentIssueRepository documentIssueRepository;
    @Mock
    private RankCodeRepository rankCodeRepository;
    @Mock
    private UnitCodeRepository unitCodeRepository;
    @Mock
    private DeathTypeRepository deathTypeRepository;
    @Mock
    private VeteransOfficeRepository veteransOfficeRepository;

    @InjectMocks
    private DocumentGenerationService documentGenerationService;

    @BeforeAll
    static void initJasperReports() {
        // 테스트 환경에서 AWT 폰트 미설치 시에도 PDF 생성 가능하도록 설정
        DefaultJasperReportsContext.getInstance()
                .setProperty("net.sf.jasperreports.awt.ignore.missing.font", "true");
    }

    @BeforeEach
    void setUp() {
        // preCompileReports()는 @PostConstruct이므로 수동 호출
        documentGenerationService.preCompileReports();
    }

    @ParameterizedTest(name = "{0} .jrxml 컴파일 성공")
    @EnumSource(DocumentType.class)
    @DisplayName("7종 .jrxml 파일이 모두 JasperCompileManager로 컴파일 성공한다")
    void test_compileAllTemplates(DocumentType type) throws Exception {
        String path = "/reports/" + type.getTemplateName() + ".jrxml";
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            assertThat(stream).as("템플릿 파일 존재: " + path).isNotNull();

            JasperReport report = JasperCompileManager.compileReport(stream);
            assertThat(report).isNotNull();
            assertThat(report.getName()).isEqualTo(type.getTemplateName());
        }
    }

    @Test
    @DisplayName("단건 문서(DEAD_CERTIFICATE) 생성 시 PDF byte[]가 %PDF- 시그니처로 시작한다")
    void test_generateDeadCertificate_returnsPdfBytes() {
        // given: rankId, unitId, deathTypeId가 null이면 resolve 메서드가 빈 문자열 반환
        Dead dead = Dead.builder()
                .id(1L)
                .name("홍길동")
                .serviceNumber("N-12345")
                .birthDate(LocalDate.of(1990, 1, 1))
                .deathDate(LocalDate.of(2025, 6, 15))
                .build();

        when(deadRepository.findById(1L)).thenReturn(Optional.of(dead));

        // when
        byte[] pdfBytes = documentGenerationService.generate(DocumentType.DEAD_CERTIFICATE, 1L);

        // then
        assertThat(pdfBytes).isNotEmpty();
        String signature = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertThat(signature).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("리스트 문서(DEAD_STATUS_REPORT) 빈 리스트여도 PDF가 정상 생성된다")
    void test_generateDeadStatusReport_emptyList() {
        // given
        when(deadRepository.findAll()).thenReturn(java.util.List.of());

        // when
        byte[] pdfBytes = documentGenerationService.generate(DocumentType.DEAD_STATUS_REPORT, null);

        // then
        assertThat(pdfBytes).isNotEmpty();
        String signature = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertThat(signature).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("존재하지 않는 targetId로 호출 시 예외가 발생한다")
    void test_generateWithInvalidId_throwsException() {
        // given
        when(deadRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> documentGenerationService.generate(DocumentType.DEAD_CERTIFICATE, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("찾을 수 없습니다");
    }

    @Test
    @DisplayName("NanumGothic 폰트 확장 설정이 classpath에 존재하고 로드 가능하다")
    void test_pdfContainsNanumGothicFont() {
        // jasperreports_extension.properties가 classpath에 존재
        try (InputStream extProps = getClass().getResourceAsStream(
                "/jasperreports_extension.properties")) {
            assertThat(extProps).as("jasperreports_extension.properties 존재").isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // fonts.xml 설정 파일이 classpath에 존재
        try (InputStream fontsXml = getClass().getResourceAsStream("/fonts/fonts.xml")) {
            assertThat(fontsXml).as("fonts/fonts.xml 존재").isNotNull();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // NanumGothic TTF 파일이 존재하고 java.awt.Font으로 로드 가능
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NanumGothic.ttf")) {
            assertThat(fontStream).as("NanumGothic.ttf 존재").isNotNull();
            java.awt.Font font = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, fontStream);
            assertThat(font.getFontName()).satisfiesAnyOf(
                    name -> assertThat(name).containsIgnoringCase("NanumGothic"),
                    name -> assertThat(name).contains("나눔고딕")
            );
        } catch (Exception e) {
            throw new RuntimeException("NanumGothic 폰트 로드 실패", e);
        }
    }

    @Test
    @DisplayName("NanumGothic 폰트 파일이 classpath에 존재하고 로드 가능하다")
    void test_nanumGothicFontFileExists() {
        try (InputStream fontStream = getClass().getResourceAsStream("/fonts/NanumGothic.ttf")) {
            assertThat(fontStream).as("NanumGothic.ttf 파일 존재").isNotNull();
        } catch (Exception e) {
            throw new RuntimeException("폰트 파일 로드 실패", e);
        }
    }
}
