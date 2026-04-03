package com.navy.casualty.document.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.document.dto.DocumentIssueRequest;
import com.navy.casualty.document.dto.DocumentIssueResponse;
import com.navy.casualty.document.dto.DocumentIssueSearchRequest;
import com.navy.casualty.document.enums.DocumentType;
import com.navy.casualty.document.service.DocumentGenerationService;
import com.navy.casualty.document.service.DocumentIssueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 문서 출력 REST API 컨트롤러.
 * PDF 생성 + 발급 이력 기록/조회를 제공한다.
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentGenerationService documentGenerationService;
    private final DocumentIssueService documentIssueService;

    /**
     * 문서를 생성하고 PDF를 반환한다.
     * 발급 이력도 자동으로 기록한다 (DOCU-08).
     */
    @PostMapping("/{documentType}/generate")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<byte[]> generateDocument(
            @PathVariable DocumentType documentType,
            @RequestParam(required = false) Long targetId,
            @Valid @RequestBody DocumentIssueRequest request) {

        // 1. 발급 이력 기록
        documentIssueService.recordIssue(documentType, targetId, request.purpose());

        // 2. PDF 생성
        byte[] pdfBytes = documentGenerationService.generate(documentType, targetId);

        // 3. PDF 응답 구성
        String encodedFileName = URLEncoder.encode(
                documentType.getFileName() + ".pdf", StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.set(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename*=UTF-8''" + encodedFileName);
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * 문서 발급 이력을 검색한다.
     */
    @GetMapping("/issues")
    @PreAuthorize("hasRole('VIEWER')")
    public ResponseEntity<ApiResponse<Page<DocumentIssueResponse>>> getIssueHistory(
            DocumentIssueSearchRequest request, Pageable pageable) {
        Page<DocumentIssueResponse> result = documentIssueService.search(request, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
