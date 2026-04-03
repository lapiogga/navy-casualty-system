package com.navy.casualty.wounded.controller;

import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.wounded.dto.WoundedCreateRequest;
import com.navy.casualty.wounded.dto.WoundedDeleteRequest;
import com.navy.casualty.wounded.dto.WoundedResponse;
import com.navy.casualty.wounded.dto.WoundedSearchRequest;
import com.navy.casualty.wounded.dto.WoundedUpdateRequest;
import com.navy.casualty.wounded.entity.WoundedStatus;
import com.navy.casualty.wounded.service.WoundedExcelService;
import com.navy.casualty.wounded.service.WoundedService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * 상이자 관리 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/wounded")
@RequiredArgsConstructor
public class WoundedController {

    private final WoundedService woundedService;
    private final WoundedExcelService woundedExcelService;

    /**
     * 상이자 목록을 검색한다 (동적 조건 + 페이징).
     */
    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<WoundedResponse>>> search(
            WoundedSearchRequest request, Pageable pageable) {
        Page<WoundedResponse> result = woundedService.search(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 상이자를 등록한다.
     */
    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<WoundedResponse>> create(
            @Valid @RequestBody WoundedCreateRequest request) {
        WoundedResponse response = woundedService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /**
     * 상이자 정보를 수정한다.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<WoundedResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody WoundedUpdateRequest request) {
        WoundedResponse response = woundedService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 상이자를 논리 삭제한다. 삭제 사유 필수.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @Valid @RequestBody WoundedDeleteRequest request) {
        woundedService.softDelete(id, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 상이자 현황을 Excel 파일로 내보낸다.
     */
    @GetMapping("/excel")
    @PreAuthorize("hasRole('VIEWER')")
    public void exportExcel(WoundedSearchRequest request, HttpServletResponse response) throws IOException {
        woundedExcelService.exportExcel(request, response);
    }

    /**
     * 상이자 상태를 변경한다.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam WoundedStatus status) {
        woundedService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
