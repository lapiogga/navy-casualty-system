package com.navy.casualty.wounded.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.code.repository.VeteransOfficeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.wounded.dto.WoundedCreateRequest;
import com.navy.casualty.wounded.dto.WoundedResponse;
import com.navy.casualty.wounded.dto.WoundedSearchRequest;
import com.navy.casualty.wounded.dto.WoundedUpdateRequest;
import com.navy.casualty.wounded.entity.Wounded;
import com.navy.casualty.wounded.entity.WoundedStatus;
import com.navy.casualty.wounded.repository.WoundedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 상이자 관리 서비스.
 * CRUD, 상태 전이, 군번/주민번호 이중 중복 검증을 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WoundedService {

    private final WoundedRepository woundedRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final VeteransOfficeRepository veteransOfficeRepository;

    /**
     * 상이자 목록을 검색한다 (동적 조건 + 페이징).
     */
    public Page<WoundedResponse> search(WoundedSearchRequest request, Pageable pageable) {
        Page<Wounded> page = woundedRepository.search(request, pageable);
        return page.map(this::toResponse);
    }

    /**
     * 상이자를 등록한다.
     */
    @AuditLog(action = "CREATE", targetTable = "TB_WOUNDED")
    @Transactional
    public WoundedResponse create(WoundedCreateRequest request) {
        // 1단계: 군번 중복 검증
        if (woundedRepository.existsByServiceNumber(request.serviceNumber())) {
            throw new IllegalArgumentException("이미 등록된 군번입니다");
        }

        // 2단계: 주민번호 해시 중복 검증
        String ssnHash = hashSsn(request.ssn());
        if (woundedRepository.existsBySsnHash(ssnHash)) {
            throw new IllegalArgumentException("이미 등록된 주민번호입니다");
        }

        // 3단계: 엔티티 생성 (PiiEncryptionConverter가 DB 저장 시 암호화)
        Wounded wounded = Wounded.builder()
                .serviceNumber(request.serviceNumber())
                .name(request.name())
                .ssnEncrypted(request.ssn())
                .ssnHash(ssnHash)
                .birthDate(request.birthDate())
                .rankId(request.rankId())
                .branchId(request.branchId())
                .unitId(request.unitId())
                .enlistmentDate(request.enlistmentDate())
                .phone(request.phone())
                .address(request.address())
                .veteransOfficeId(request.veteransOfficeId())
                .diseaseName(request.diseaseName())
                .woundType(request.woundType())
                .build();

        Wounded saved = woundedRepository.save(wounded);
        return toResponse(saved);
    }

    /**
     * 상이자 정보를 수정한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_WOUNDED")
    @Transactional
    public WoundedResponse update(Long id, WoundedUpdateRequest request) {
        Wounded wounded = woundedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상이자를 찾을 수 없습니다"));

        wounded.update(request);

        // 주민번호 변경 시 해시도 재계산
        if (request.ssn() != null && !request.ssn().isBlank()) {
            String newHash = hashSsn(request.ssn());
            wounded.updateSsn(request.ssn(), newHash);
        }

        return toResponse(wounded);
    }

    /**
     * 상이자를 논리 삭제한다.
     */
    @AuditLog(action = "DELETE", targetTable = "TB_WOUNDED")
    @Transactional
    public void softDelete(Long id, String reason) {
        Wounded wounded = woundedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상이자를 찾을 수 없습니다"));

        String currentUser = getCurrentUsername();
        wounded.softDelete(currentUser, reason);
    }

    /**
     * 상이자 상태를 전이한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_WOUNDED")
    @Transactional
    public void updateStatus(Long id, WoundedStatus newStatus) {
        Wounded wounded = woundedRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상이자를 찾을 수 없습니다"));

        wounded.updateStatus(newStatus);
    }

    /**
     * Wounded 엔티티를 WoundedResponse로 변환한다.
     * 코드 테이블에서 이름을 조회하고, 역할에 따라 주민번호를 마스킹한다.
     */
    private WoundedResponse toResponse(Wounded wounded) {
        String role = getCurrentUserRole();
        String maskedSsn = RrnMaskingUtil.mask(wounded.getSsnEncrypted(), role);

        String rankName = wounded.getRankId() != null
                ? rankCodeRepository.findById(wounded.getRankId()).map(r -> r.getRankName()).orElse("")
                : "";
        String branchName = wounded.getBranchId() != null
                ? branchCodeRepository.findById(wounded.getBranchId()).map(b -> b.getBranchName()).orElse("")
                : "";
        String unitName = wounded.getUnitId() != null
                ? unitCodeRepository.findById(wounded.getUnitId()).map(u -> u.getUnitName()).orElse("")
                : "";
        String veteransOfficeName = wounded.getVeteransOfficeId() != null
                ? veteransOfficeRepository.findById(wounded.getVeteransOfficeId()).map(v -> v.getOfficeName()).orElse("")
                : "";

        return WoundedResponse.from(wounded, maskedSsn, rankName, branchName, unitName, veteransOfficeName);
    }

    /**
     * 현재 인증된 사용자의 역할을 반환한다.
     */
    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            return "ROLE_VIEWER";
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .orElse("ROLE_VIEWER");
    }

    /**
     * 현재 인증된 사용자명을 반환한다.
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return "system";
        }
        return auth.getName();
    }

    /**
     * 주민번호를 SHA-256 해시하여 hex 문자열로 반환한다.
     */
    private String hashSsn(String ssn) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(ssn.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다", e);
        }
    }
}
