package com.navy.casualty.review.controller;

import java.io.IOException;
import java.util.List;

import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.review.dto.ReviewCreateRequest;
import com.navy.casualty.review.dto.ReviewDeleteRequest;
import com.navy.casualty.review.dto.ReviewHistoryResponse;
import com.navy.casualty.review.dto.ReviewResponse;
import com.navy.casualty.review.dto.ReviewSearchRequest;
import com.navy.casualty.review.dto.ReviewUpdateRequest;
import com.navy.casualty.review.entity.ReviewStatus;
import com.navy.casualty.review.service.ReviewExcelService;
import com.navy.casualty.review.service.ReviewService;
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

/**
 * 전공사상심사 관리 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewExcelService reviewExcelService;

    /**
     * 검색 조건이 적용된 전공사상심사 현황을 Excel로 내보낸다.
     */
    @GetMapping("/excel")
    @PreAuthorize("hasRole('VIEWER')")
    public void exportExcel(ReviewSearchRequest request, HttpServletResponse response) throws IOException {
        reviewExcelService.exportExcel(request, response);
    }

    /**
     * 심사 목록을 검색한다 (동적 조건 + 페이징).
     */
    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> search(
            ReviewSearchRequest request, Pageable pageable) {
        Page<ReviewResponse> result = reviewService.search(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    /**
     * 심사를 등록한다.
     */
    @PostMapping
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewCreateRequest request) {
        ReviewResponse response = reviewService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response));
    }

    /**
     * 심사 정보를 수정한다.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {
        ReviewResponse response = reviewService.update(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 심사를 논리 삭제한다. 삭제 사유 필수.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @Valid @RequestBody ReviewDeleteRequest request) {
        reviewService.softDelete(id, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 심사 상태를 변경한다.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @PathVariable Long id,
            @RequestParam ReviewStatus status) {
        reviewService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /**
     * 심사 이력 목록을 조회한다 (시간순).
     */
    @GetMapping("/{id}/histories")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<List<ReviewHistoryResponse>>> getHistories(
            @PathVariable Long id) {
        List<ReviewHistoryResponse> histories = reviewService.getHistories(id);
        return ResponseEntity.ok(ApiResponse.ok(histories));
    }

    /**
     * 보훈청 통보 일시를 기록한다.
     */
    @PutMapping("/{id}/notify")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<ReviewResponse>> recordNotification(
            @PathVariable Long id) {
        ReviewResponse response = reviewService.recordNotification(id);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
