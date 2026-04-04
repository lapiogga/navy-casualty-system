package com.navy.casualty.audit.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 감사 로그 월별 보고서 생성 서비스.
 * JdbcTemplate으로 집계 쿼리를 실행하고 JasperReports로 PDF를 렌더링한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditReportService {

    private final JdbcTemplate jdbcTemplate;
    private final ConcurrentHashMap<String, JasperReport> reportCache = new ConcurrentHashMap<>();

    private static final String TEMPLATE_PATH = "/reports/audit_monthly_report.jrxml";

    /**
     * 월별 감사 보고서를 PDF로 생성한다.
     *
     * @param year  연도
     * @param month 월 (1~12)
     * @return PDF 바이트 배열
     */
    public byte[] generateMonthlyReport(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1);

        // 총 접근 건수
        Long totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tb_audit_log WHERE created_at >= ? AND created_at < ?",
                Long.class, startDate, endDate);

        // 작업 유형별 건수
        List<Map<String, Object>> actionStats = jdbcTemplate.queryForList(
                "SELECT action, COUNT(*) as cnt FROM tb_audit_log " +
                "WHERE created_at >= ? AND created_at < ? " +
                "GROUP BY action ORDER BY cnt DESC",
                startDate, endDate);

        // 사용자별 활동 건수 (상위 10명)
        List<Map<String, Object>> userStats = jdbcTemplate.queryForList(
                "SELECT user_id, COUNT(*) as cnt FROM tb_audit_log " +
                "WHERE created_at >= ? AND created_at < ? " +
                "GROUP BY user_id ORDER BY cnt DESC LIMIT 10",
                startDate, endDate);

        // 삭제 이력 목록
        List<Map<String, Object>> deleteHistory = jdbcTemplate.queryForList(
                "SELECT user_id, target_table, target_id, detail, created_at FROM tb_audit_log " +
                "WHERE created_at >= ? AND created_at < ? AND action = 'DELETE' " +
                "ORDER BY created_at DESC",
                startDate, endDate);

        // JasperReport 데이터소스 구성 (작업유형별 행을 메인 데이터소스로 사용)
        List<Map<String, Object>> reportRows = new ArrayList<>();
        for (Map<String, Object> row : actionStats) {
            Map<String, Object> r = new HashMap<>();
            r.put("actionType", String.valueOf(row.get("action")));
            r.put("actionCount", String.valueOf(row.get("cnt")));
            reportRows.add(r);
        }

        // 데이터가 없으면 빈 행 추가 (JasperReports 빈 데이터소스 방지)
        if (reportRows.isEmpty()) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("actionType", "N/A");
            empty.put("actionCount", "0");
            reportRows.add(empty);
        }

        // 파라미터 구성
        Map<String, Object> params = new HashMap<>();
        params.put("reportTitle", String.format("감사 로그 월별 보고서 - %d년 %d월", year, month));
        params.put("totalCount", totalCount != null ? totalCount.toString() : "0");
        params.put("reportPeriod", String.format("%d-%02d-01 ~ %d-%02d", year, month, year, month));

        // 사용자별 서브 데이터소스
        List<Map<String, Object>> userRows = new ArrayList<>();
        for (Map<String, Object> row : userStats) {
            Map<String, Object> r = new HashMap<>();
            r.put("userId", String.valueOf(row.get("user_id")));
            r.put("userCount", String.valueOf(row.get("cnt")));
            userRows.add(r);
        }
        params.put("userDataSource", new JRBeanCollectionDataSource(userRows));

        // 삭제 이력 서브 데이터소스
        List<Map<String, Object>> deleteRows = new ArrayList<>();
        for (Map<String, Object> row : deleteHistory) {
            Map<String, Object> r = new HashMap<>();
            r.put("deleteUserId", String.valueOf(row.get("user_id")));
            r.put("deleteTable", String.valueOf(row.get("target_table")));
            r.put("deleteTargetId", String.valueOf(row.get("target_id")));
            r.put("deleteDetail", row.get("detail") != null ? String.valueOf(row.get("detail")) : "");
            r.put("deleteDate", String.valueOf(row.get("created_at")));
            deleteRows.add(r);
        }
        params.put("deleteDataSource", new JRBeanCollectionDataSource(deleteRows));

        try {
            JasperReport report = reportCache.computeIfAbsent("audit_monthly", k -> compileReport());
            JRBeanCollectionDataSource mainDs = new JRBeanCollectionDataSource(reportRows);
            JasperPrint print = JasperFillManager.fillReport(report, params, mainDs);
            return JasperExportManager.exportReportToPdf(print);
        } catch (JRException e) {
            throw new IllegalStateException("감사 보고서 PDF 생성 실패", e);
        }
    }

    /**
     * .jrxml 템플릿을 컴파일한다.
     */
    private JasperReport compileReport() {
        try (InputStream stream = getClass().getResourceAsStream(TEMPLATE_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("감사 보고서 템플릿을 찾을 수 없습니다: " + TEMPLATE_PATH);
            }
            return JasperCompileManager.compileReport(stream);
        } catch (JRException e) {
            throw new IllegalStateException("감사 보고서 템플릿 컴파일 실패", e);
        } catch (Exception e) {
            throw new IllegalStateException("감사 보고서 템플릿 로드 실패", e);
        }
    }
}
