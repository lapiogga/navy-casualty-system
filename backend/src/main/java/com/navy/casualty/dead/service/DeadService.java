package com.navy.casualty.dead.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.DeathCodeRepository;
import com.navy.casualty.code.repository.DeathTypeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.dead.dto.DeadCreateRequest;
import com.navy.casualty.dead.dto.DeadResponse;
import com.navy.casualty.dead.dto.DeadSearchRequest;
import com.navy.casualty.dead.dto.DeadUpdateRequest;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.entity.DeadStatus;
import com.navy.casualty.dead.repository.DeadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사망자 관리 서비스.
 * CRUD, 상태 전이, 군번/주민번호 이중 중복 검증을 처리한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeadService {

    private final DeadRepository deadRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final DeathTypeRepository deathTypeRepository;
    private final DeathCodeRepository deathCodeRepository;
    private final UnitCodeRepository unitCodeRepository;

    /**
     * 사망자 목록을 검색한다 (동적 조건 + 페이징).
     */
    public Page<DeadResponse> search(DeadSearchRequest request, Pageable pageable) {
        Page<Dead> page = deadRepository.search(request, pageable);
        return page.map(this::toResponse);
    }

    /**
     * 사망자를 등록한다.
     */
    @AuditLog(action = "CREATE", targetTable = "TB_DEAD")
    @Transactional
    public DeadResponse create(DeadCreateRequest request) {
        // 1단계: 군번 중복 검증
        if (deadRepository.existsByServiceNumber(request.serviceNumber())) {
            throw new IllegalArgumentException("이미 등록된 군번입니다");
        }

        // 2단계: 주민번호 해시 중복 검증 (DEAD-07)
        String ssnHash = hashSsn(request.ssn());
        if (deadRepository.existsBySsnHash(ssnHash)) {
            throw new IllegalArgumentException("이미 등록된 주민번호입니다");
        }

        // 3단계: 엔티티 생성 (PiiEncryptionConverter가 DB 저장 시 암호화)
        Dead dead = Dead.builder()
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
                .deathTypeId(request.deathTypeId())
                .deathCodeId(request.deathCodeId())
                .address(request.address())
                .deathDate(request.deathDate())
                .build();

        Dead saved = deadRepository.save(dead);
        return toResponse(saved);
    }

    /**
     * 사망자 정보를 수정한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_DEAD")
    @Transactional
    public DeadResponse update(Long id, DeadUpdateRequest request) {
        Dead dead = deadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사망자를 찾을 수 없습니다"));

        dead.update(request);

        // 주민번호 변경 시 해시도 재계산
        if (request.ssn() != null && !request.ssn().isBlank()) {
            String newHash = hashSsn(request.ssn());
            dead.updateSsn(request.ssn(), newHash);
        }

        return toResponse(dead);
    }

    /**
     * 사망자를 논리 삭제한다.
     */
    @AuditLog(action = "DELETE", targetTable = "TB_DEAD")
    @Transactional
    public void softDelete(Long id, String reason) {
        Dead dead = deadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사망자를 찾을 수 없습니다"));

        String currentUser = getCurrentUsername();
        dead.softDelete(currentUser, reason);
    }

    /**
     * 사망자 상태를 전이한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_DEAD")
    @Transactional
    public void updateStatus(Long id, DeadStatus newStatus) {
        Dead dead = deadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("사망자를 찾을 수 없습니다"));

        dead.updateStatus(newStatus);
    }

    /**
     * Dead 엔티티를 DeadResponse로 변환한다.
     * 코드 테이블에서 이름을 조회하고, 역할에 따라 주민번호를 마스킹한다.
     */
    private DeadResponse toResponse(Dead dead) {
        String role = getCurrentUserRole();
        String maskedSsn = RrnMaskingUtil.mask(dead.getSsnEncrypted(), role);

        String rankName = dead.getRankId() != null
                ? rankCodeRepository.findById(dead.getRankId()).map(r -> r.getRankName()).orElse("")
                : "";
        String branchName = dead.getBranchId() != null
                ? branchCodeRepository.findById(dead.getBranchId()).map(b -> b.getBranchName()).orElse("")
                : "";
        String unitName = dead.getUnitId() != null
                ? unitCodeRepository.findById(dead.getUnitId()).map(u -> u.getUnitName()).orElse("")
                : "";
        String deathTypeName = dead.getDeathTypeId() != null
                ? deathTypeRepository.findById(dead.getDeathTypeId()).map(t -> t.getTypeName()).orElse("")
                : "";
        String deathCodeSymbol = dead.getDeathCodeId() != null
                ? deathCodeRepository.findById(dead.getDeathCodeId()).map(c -> c.getCodeSymbol()).orElse("")
                : "";

        return DeadResponse.from(dead, maskedSsn, rankName, branchName, unitName, deathTypeName, deathCodeSymbol);
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
