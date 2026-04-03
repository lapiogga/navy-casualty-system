package com.navy.casualty.dead.controller;

import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.dead.dto.DeadCreateRequest;
import com.navy.casualty.dead.dto.DeadDeleteRequest;
import com.navy.casualty.dead.dto.DeadResponse;
import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.dto.DeadUpdateRequest;
import com.navy.casualty.dead.entity.DeadStatus;
import com.navy.casualty.dead.service.DeadService;
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

/**
 * 사망자 관리 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/dead")
@RequiredArgsConstructor
public class DeadController {

    private final DeadService deadService;

    /**
     * 사망자 목록을 검색한다 (동적 조건 + 페이징).
     */
    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<DeadResponse>>> search(
            DeadSearchRequest request, Pageable pageable) {
        Page<DeadResponse> result = deadService.search(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 사망자를 등록한다.
     */
    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<DeadResponse>> create(
            @Valid @RequestBody DeadCreateRequest request) {
        DeadResponse response = deadService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /**
     * 사망자 정보를 수정한다.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<DeadResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DeadUpdateRequest request) {
        DeadResponse response = deadService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 사망자를 논리 삭제한다. 삭제 사유 필수.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @Valid @RequestBody DeadDeleteRequest request) {
        deadService.softDelete(id, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 사망자 상태를 변경한다.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam DeadStatus status) {
        deadService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
