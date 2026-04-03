package com.navy.casualty.code.controller;

import java.util.List;

import com.navy.casualty.code.entity.BranchCode;
import com.navy.casualty.code.entity.DeathCode;
import com.navy.casualty.code.entity.DeathType;
import com.navy.casualty.code.entity.RankCode;
import com.navy.casualty.code.entity.UnitCode;
import com.navy.casualty.code.entity.VeteransOffice;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코드 테이블 조회 API. Select 드롭다운용 데이터를 반환한다.
 */
@RestController
@RequestMapping("/api/codes")
@PreAuthorize("hasRole('VIEWER')")
@RequiredArgsConstructor
public class CodeController {

    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final DeathTypeRepository deathTypeRepository;
    private final DeathCodeRepository deathCodeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final VeteransOfficeRepository veteransOfficeRepository;

    @GetMapping("/ranks")
    public ResponseEntity<ApiResponse<List<RankCode>>> getRanks() {
        return ResponseEntity.ok(ApiResponse.ok(rankCodeRepository.findAllByOrderBySortOrderAsc()));
    }

    @GetMapping("/branches")
    public ResponseEntity<ApiResponse<List<BranchCode>>> getBranches() {
        return ResponseEntity.ok(ApiResponse.ok(branchCodeRepository.findAll()));
    }

    @GetMapping("/death-types")
    public ResponseEntity<ApiResponse<List<DeathType>>> getDeathTypes() {
        return ResponseEntity.ok(ApiResponse.ok(deathTypeRepository.findAll()));
    }

    @GetMapping("/death-codes")
    public ResponseEntity<ApiResponse<List<DeathCode>>> getDeathCodes() {
        return ResponseEntity.ok(ApiResponse.ok(deathCodeRepository.findAll()));
    }

    @GetMapping("/units")
    public ResponseEntity<ApiResponse<List<UnitCode>>> getUnits() {
        return ResponseEntity.ok(ApiResponse.ok(unitCodeRepository.findAll()));
    }

    @GetMapping("/veterans-offices")
    public ResponseEntity<ApiResponse<List<VeteransOffice>>> getVeteransOffices() {
        return ResponseEntity.ok(ApiResponse.ok(veteransOfficeRepository.findAll()));
    }
}
