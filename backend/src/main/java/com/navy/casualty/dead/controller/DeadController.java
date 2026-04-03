package com.navy.casualty.dead.controller;

import java.io.IOException;

import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.service.DeadExcelService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사망자 관리 REST API 컨트롤러.
 */
@RestController
@RequestMapping("/api/dead")
@RequiredArgsConstructor
public class DeadController {

    private final DeadExcelService deadExcelService;

    /**
     * 사망자 현황 Excel 다운로드.
     * 검색 조건이 적용된 결과를 .xlsx 파일로 반환한다.
     */
    @GetMapping("/excel")
    @PreAuthorize("hasRole('VIEWER')")
    public void exportExcel(DeadSearchRequest request, HttpServletResponse response) throws IOException {
        deadExcelService.exportExcel(request, response);
    }
}
