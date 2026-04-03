package com.navy.casualty.document.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.repository.DeadRepository;
import com.navy.casualty.document.dto.DocumentIssueSearchRequest;
import com.navy.casualty.document.entity.DocumentIssue;
import com.navy.casualty.document.enums.DocumentType;
import com.navy.casualty.document.repository.DocumentIssueRepository;
import com.navy.casualty.review.entity.Review;
import com.navy.casualty.review.repository.ReviewRepository;
import com.navy.casualty.wounded.entity.Wounded;
import com.navy.casualty.wounded.repository.WoundedRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

/**
 * JasperReports 기반 PDF 문서 생성 서비스.
 * .jrxml 컴파일 결과를 캐싱하여 반복 컴파일 비용을 제거한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentGenerationService {

    private final DeadRepository deadRepository;
    private final WoundedRepository woundedRepository;
    private final ReviewRepository reviewRepository;
    private final DocumentIssueRepository documentIssueRepository;
    private final RankCodeRepository rankCodeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final DeathTypeRepository deathTypeRepository;
    private final VeteransOfficeRepository veteransOfficeRepository;

    private final ConcurrentHashMap<DocumentType, JasperReport> reportCache = new ConcurrentHashMap<>();

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 애플리케이션 시작 시 7종 .jrxml 템플릿을 미리 컴파일한다.
     */
    @PostConstruct
    public void preCompileReports() {
        for (DocumentType type : DocumentType.values()) {
            try {
                JasperReport report = compileReport(type);
                reportCache.put(type, report);
                log.info("JasperReport 컴파일 완료: {}", type.getTemplateName());
            } catch (Exception e) {
                log.error("JasperReport 컴파일 실패: {}", type.getTemplateName(), e);
            }
        }
    }

    /**
     * 문서 유형과 대상 ID로 PDF byte[]를 생성한다.
     */
    public byte[] generate(DocumentType type, Long targetId) {
        try {
            JasperReport report = reportCache.computeIfAbsent(type, this::compileReport);

            JasperPrint print;

            if (isListReport(type)) {
                JRBeanCollectionDataSource dataSource = buildListDataSource(type);
                Map<String, Object> params = new HashMap<>();
                params.put("issueDate", LocalDate.now().format(DATE_FORMAT));
                print = JasperFillManager.fillReport(report, params, dataSource);
            } else {
                Map<String, Object> params = buildParameters(type, targetId);
                print = JasperFillManager.fillReport(report, params, new JREmptyDataSource());
            }

            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) {
            throw new IllegalStateException("문서 생성 실패: " + type, e);
        }
    }

    /**
     * 리스트형 보고서인지 판별한다.
     */
    private boolean isListReport(DocumentType type) {
        return type == DocumentType.DEAD_STATUS_REPORT
                || type == DocumentType.WOUNDED_STATUS_REPORT
                || type == DocumentType.ISSUE_LEDGER;
    }

    /**
     * .jrxml 파일을 컴파일한다.
     */
    private JasperReport compileReport(DocumentType type) {
        String path = "/reports/" + type.getTemplateName() + ".jrxml";
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalStateException("템플릿 파일을 찾을 수 없습니다: " + path);
            }
            return JasperCompileManager.compileReport(stream);
        } catch (JRException e) {
            throw new IllegalStateException("템플릿 컴파일 실패: " + path, e);
        } catch (Exception e) {
            throw new IllegalStateException("템플릿 로드 실패: " + path, e);
        }
    }

    /**
     * 단건 문서의 파라미터를 구성한다.
     */
    private Map<String, Object> buildParameters(DocumentType type, Long targetId) {
        Map<String, Object> params = new HashMap<>();
        params.put("issueDate", LocalDate.now().format(DATE_FORMAT));

        switch (type) {
            case DEAD_CERTIFICATE -> buildDeadCertificateParams(targetId, params);
            case WOUNDED_CERTIFICATE -> buildWoundedCertificateParams(targetId, params);
            case REVIEW_RESULT -> buildReviewResultParams(targetId, params);
            case DEATH_CONFIRMATION -> buildDeathConfirmationParams(targetId, params);
            default -> throw new IllegalArgumentException("단건 문서가 아닙니다: " + type);
        }

        return params;
    }

    /**
     * 국가유공자 확인서(사망자) 파라미터를 구성한다.
     */
    private void buildDeadCertificateParams(Long targetId, Map<String, Object> params) {
        Dead dead = deadRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사망자를 찾을 수 없습니다: " + targetId));

        params.put("name", dead.getName());
        params.put("serviceNumber", dead.getServiceNumber());
        params.put("rankName", resolveRankName(dead.getRankId()));
        params.put("birthDate", formatDate(dead.getBirthDate()));
        params.put("deathDate", formatDate(dead.getDeathDate()));
        params.put("deathTypeName", resolveDeathTypeName(dead.getDeathTypeId()));
        params.put("unitName", resolveUnitName(dead.getUnitId()));
    }

    /**
     * 국가유공자 확인서(상이자) 파라미터를 구성한다.
     */
    private void buildWoundedCertificateParams(Long targetId, Map<String, Object> params) {
        Wounded wounded = woundedRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("상이자를 찾을 수 없습니다: " + targetId));

        params.put("name", wounded.getName());
        params.put("serviceNumber", wounded.getServiceNumber());
        params.put("rankName", resolveRankName(wounded.getRankId()));
        params.put("birthDate", formatDate(wounded.getBirthDate()));
        params.put("diseaseName", wounded.getDiseaseName() != null ? wounded.getDiseaseName() : "");
        params.put("woundTypeName", wounded.getWoundType() != null ? wounded.getWoundType().name() : "");
        params.put("veteransOfficeName", resolveVeteransOfficeName(wounded.getVeteransOfficeId()));
        params.put("unitName", resolveUnitName(wounded.getUnitId()));
    }

    /**
     * 전공사상심사결과서 파라미터를 구성한다.
     */
    private void buildReviewResultParams(Long targetId, Map<String, Object> params) {
        Review review = reviewRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("심사 정보를 찾을 수 없습니다: " + targetId));

        params.put("name", review.getName());
        params.put("serviceNumber", review.getServiceNumber());
        params.put("rankName", resolveRankName(review.getRankId()));
        params.put("reviewRound", review.getReviewRound() != null ? String.valueOf(review.getReviewRound()) : "");
        params.put("reviewDate", review.getReviewDate() != null ? review.getReviewDate().format(DATE_FORMAT) : "");
        params.put("classification", review.getClassification() != null ? review.getClassification().name() : "");
        params.put("unitReviewResult", review.getUnitReviewResult() != null ? review.getUnitReviewResult() : "");
        params.put("diseaseName", review.getDiseaseName() != null ? review.getDiseaseName() : "");
    }

    /**
     * 순직/사망확인서 파라미터를 구성한다.
     */
    private void buildDeathConfirmationParams(Long targetId, Map<String, Object> params) {
        Dead dead = deadRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("사망자를 찾을 수 없습니다: " + targetId));

        params.put("name", dead.getName());
        params.put("serviceNumber", dead.getServiceNumber());
        params.put("rankName", resolveRankName(dead.getRankId()));
        params.put("birthDate", formatDate(dead.getBirthDate()));
        params.put("deathDate", formatDate(dead.getDeathDate()));
        params.put("deathTypeName", resolveDeathTypeName(dead.getDeathTypeId()));
        params.put("enlistmentDate", formatDate(dead.getEnlistmentDate()));
        params.put("unitName", resolveUnitName(dead.getUnitId()));
    }

    /**
     * 리스트형 보고서의 데이터 소스를 구성한다.
     */
    private JRBeanCollectionDataSource buildListDataSource(DocumentType type) {
        return switch (type) {
            case DEAD_STATUS_REPORT -> {
                List<Map<String, Object>> rows = deadRepository.findAll().stream()
                        .map(this::deadToRow)
                        .toList();
                yield new JRBeanCollectionDataSource(rows);
            }
            case WOUNDED_STATUS_REPORT -> {
                List<Map<String, Object>> rows = woundedRepository.findAll().stream()
                        .map(this::woundedToRow)
                        .toList();
                yield new JRBeanCollectionDataSource(rows);
            }
            case ISSUE_LEDGER -> {
                List<Map<String, Object>> rows = documentIssueRepository.findAll().stream()
                        .map(this::issueToRow)
                        .toList();
                yield new JRBeanCollectionDataSource(rows);
            }
            default -> throw new IllegalArgumentException("리스트 보고서가 아닙니다: " + type);
        };
    }

    /**
     * Dead 엔티티를 보고서 행 Map으로 변환한다.
     */
    private Map<String, Object> deadToRow(Dead dead) {
        Map<String, Object> row = new HashMap<>();
        row.put("serviceNumber", dead.getServiceNumber());
        row.put("name", dead.getName());
        row.put("rankName", resolveRankName(dead.getRankId()));
        row.put("unitName", resolveUnitName(dead.getUnitId()));
        row.put("deathDate", formatDate(dead.getDeathDate()));
        row.put("deathTypeName", resolveDeathTypeName(dead.getDeathTypeId()));
        return row;
    }

    /**
     * Wounded 엔티티를 보고서 행 Map으로 변환한다.
     */
    private Map<String, Object> woundedToRow(Wounded wounded) {
        Map<String, Object> row = new HashMap<>();
        row.put("serviceNumber", wounded.getServiceNumber());
        row.put("name", wounded.getName());
        row.put("rankName", resolveRankName(wounded.getRankId()));
        row.put("unitName", resolveUnitName(wounded.getUnitId()));
        row.put("diseaseName", wounded.getDiseaseName() != null ? wounded.getDiseaseName() : "");
        row.put("woundTypeName", wounded.getWoundType() != null ? wounded.getWoundType().name() : "");
        row.put("veteransOfficeName", resolveVeteransOfficeName(wounded.getVeteransOfficeId()));
        return row;
    }

    /**
     * DocumentIssue 엔티티를 발급대장 행 Map으로 변환한다.
     */
    private Map<String, Object> issueToRow(DocumentIssue issue) {
        Map<String, Object> row = new HashMap<>();
        row.put("documentTypeName", issue.getDocumentType().getFileName());
        row.put("targetInfo", issue.getTargetTable() + "#" + issue.getTargetId());
        row.put("issuePurpose", issue.getIssuePurpose());
        row.put("issuedBy", issue.getIssuedBy());
        row.put("issuedAt", issue.getIssuedAt() != null
                ? issue.getIssuedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "");
        return row;
    }

    private String resolveRankName(Long rankId) {
        if (rankId == null) return "";
        return rankCodeRepository.findById(rankId).map(r -> r.getRankName()).orElse("");
    }

    private String resolveUnitName(Long unitId) {
        if (unitId == null) return "";
        return unitCodeRepository.findById(unitId).map(u -> u.getUnitName()).orElse("");
    }

    private String resolveDeathTypeName(Long deathTypeId) {
        if (deathTypeId == null) return "";
        return deathTypeRepository.findById(deathTypeId).map(t -> t.getTypeName()).orElse("");
    }

    private String resolveVeteransOfficeName(Long veteransOfficeId) {
        if (veteransOfficeId == null) return "";
        return veteransOfficeRepository.findById(veteransOfficeId).map(v -> v.getOfficeName()).orElse("");
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }
}
