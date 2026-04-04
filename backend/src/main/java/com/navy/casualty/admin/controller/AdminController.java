package com.navy.casualty.admin.controller;

import com.navy.casualty.admin.dto.DataCheckResponse;
import com.navy.casualty.admin.dto.ImportResultResponse;
import com.navy.casualty.admin.service.DataImportService;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 관리자 전용 REST API 컨트롤러.
 * ADMIN 역할만 접근 가능 (SecurityConfig에서 /api/admin/** hasRole ADMIN 설정).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final DeathTypeRepository deathTypeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final VeteransOfficeRepository veteransOfficeRepository;
    private final UserRepository userRepository;
    private final DataImportService dataImportService;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    /**
     * 데이터 정합성 확인.
     * 각 코드 테이블 COUNT + admin 존재 여부 + app.version 반환.
     */
    @GetMapping("/data-check")
    public ResponseEntity<ApiResponse<DataCheckResponse>> dataCheck() {
        DataCheckResponse response = new DataCheckResponse(
                rankCodeRepository.count(),
                branchCodeRepository.count(),
                deathTypeRepository.count(),
                unitCodeRepository.count(),
                veteransOfficeRepository.count(),
                userRepository.findByUsername("admin").isPresent(),
                appVersion
        );
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Excel 파일 임포트.
     * 사망자/상이자/심사 데이터를 일괄 등록한다.
     *
     * @param type 임포트 타입 (dead, wounded, review)
     * @param file Excel 파일 (.xlsx)
     */
    @PostMapping("/import/{type}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ImportResultResponse>> importExcel(
            @PathVariable String type,
            @RequestParam("file") MultipartFile file) {
        ImportResultResponse result = dataImportService.importExcel(type, file);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 임포트 오류 리포트 다운로드.
     * 오류 목록을 Excel 파일로 반환한다.
     */
    @PostMapping("/import/error-report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> downloadErrorReport(
            @RequestParam("type") String type,
            @RequestParam("file") MultipartFile file) {
        ImportResultResponse result = dataImportService.importExcel(type, file);
        if (result.errorRows() == 0) {
            return ResponseEntity.noContent().build();
        }
        byte[] report = dataImportService.generateErrorReport(result.errors());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"import-errors.xlsx\"")
                .body(report);
    }
}
