package com.navy.casualty.admin.controller;

import com.navy.casualty.admin.dto.DataCheckResponse;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.common.dto.ApiResponse;
import com.navy.casualty.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
