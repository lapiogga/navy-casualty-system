package com.navy.casualty.review.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navy.casualty.audit.annotation.AuditLog;
import com.navy.casualty.code.repository.BranchCodeRepository;
import com.navy.casualty.code.repository.RankCodeRepository;
import com.navy.casualty.code.repository.UnitCodeRepository;
import com.navy.casualty.common.crypto.RrnMaskingUtil;
import com.navy.casualty.dead.entity.Dead;
import com.navy.casualty.dead.entity.DeadStatus;
import com.navy.casualty.dead.repository.DeadRepository;
import com.navy.casualty.review.dto.ReviewCreateRequest;
import com.navy.casualty.review.dto.ReviewHistoryResponse;
import com.navy.casualty.review.dto.ReviewResponse;
import com.navy.casualty.review.dto.ReviewSearchRequest;
import com.navy.casualty.review.dto.ReviewSnapshot;
import com.navy.casualty.review.dto.ReviewUpdateRequest;
import com.navy.casualty.review.entity.Review;
import com.navy.casualty.review.entity.ReviewClassification;
import com.navy.casualty.review.entity.ReviewHistory;
import com.navy.casualty.review.entity.ReviewStatus;
import com.navy.casualty.review.repository.ReviewHistoryRepository;
import com.navy.casualty.review.repository.ReviewRepository;
import com.navy.casualty.wounded.entity.Wounded;
import com.navy.casualty.wounded.entity.WoundType;
import com.navy.casualty.wounded.entity.WoundedStatus;
import com.navy.casualty.wounded.repository.WoundedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전공사상심사 관리 서비스.
 * CRUD + 이력 스냅샷 자동저장 + Dead/Wounded 자동반영 + 보훈청 통보를 처리한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final RankCodeRepository rankCodeRepository;
    private final BranchCodeRepository branchCodeRepository;
    private final UnitCodeRepository unitCodeRepository;
    private final DeadRepository deadRepository;
    private final WoundedRepository woundedRepository;
    private final ObjectMapper objectMapper;

    /**
     * 심사 목록을 검색한다 (동적 조건 + 페이징).
     */
    public Page<ReviewResponse> search(ReviewSearchRequest request, Pageable pageable) {
        Page<Review> page = reviewRepository.search(request, pageable);
        return page.map(this::toResponse);
    }

    /**
     * 심사를 등록한다.
     */
    @AuditLog(action = "CREATE", targetTable = "TB_REVIEW")
    @Transactional
    public ReviewResponse create(ReviewCreateRequest request) {
        // 군번+심사차수 중복 검증
        if (reviewRepository.existsByServiceNumberAndReviewRound(
                request.serviceNumber(), request.reviewRound())) {
            throw new IllegalArgumentException("동일 군번+심사차수 조합이 이미 존재합니다");
        }

        // 주민번호 해시 계산
        String ssnHash = null;
        if (request.ssn() != null && !request.ssn().isBlank()) {
            ssnHash = hashSsn(request.ssn());
        }

        Review review = Review.builder()
                .reviewRound(request.reviewRound())
                .reviewDate(request.reviewDate())
                .name(request.name())
                .serviceNumber(request.serviceNumber())
                .ssnEncrypted(request.ssn())
                .ssnHash(ssnHash)
                .birthDate(request.birthDate())
                .rankId(request.rankId())
                .branchId(request.branchId())
                .unitId(request.unitId())
                .enlistmentDate(request.enlistmentDate())
                .diseaseName(request.diseaseName())
                .unitReviewResult(request.unitReviewResult())
                .classification(request.classification())
                .build();

        Review saved = reviewRepository.save(review);

        // 등록 시 classification이 있으면 자동반영
        if (saved.getClassification() != null) {
            applyClassificationToRecord(saved);
        }

        return toResponse(saved);
    }

    /**
     * 심사 정보를 수정한다.
     * 변경 전 스냅샷을 TB_REVIEW_HISTORY에 JSONB로 자동 저장한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_REVIEW")
    @Transactional
    public ReviewResponse update(Long id, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("심사 정보를 찾을 수 없습니다"));

        // (1) 변경 전 스냅샷 저장
        saveSnapshot(review);

        // (2) 이전 classification 임시 저장 (자동반영 비교용)
        ReviewClassification previousClassification = review.getClassification();

        // (3) 심사 정보 업데이트
        review.update(request);

        // (4) 주민번호 변경 시 해시 재계산
        if (request.ssn() != null && !request.ssn().isBlank()) {
            String newHash = hashSsn(request.ssn());
            review.updateSsn(request.ssn(), newHash);
        }

        // (5) classification이 변경된 경우 자동반영
        if (review.getClassification() != null
                && review.getClassification() != previousClassification) {
            applyClassificationToRecord(review);
        }

        return toResponse(review);
    }

    /**
     * 심사를 논리 삭제한다.
     */
    @AuditLog(action = "DELETE", targetTable = "TB_REVIEW")
    @Transactional
    public void softDelete(Long id, String reason) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("심사 정보를 찾을 수 없습니다"));

        String currentUser = getCurrentUsername();
        review.softDelete(currentUser, reason);
    }

    /**
     * 심사 상태를 전이한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_REVIEW")
    @Transactional
    public void updateStatus(Long id, ReviewStatus newStatus) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("심사 정보를 찾을 수 없습니다"));

        review.updateStatus(newStatus);
    }

    /**
     * 보훈청 통보 일시를 기록하고 상태를 NOTIFIED로 전이한다.
     */
    @AuditLog(action = "UPDATE", targetTable = "TB_REVIEW")
    @Transactional
    public ReviewResponse recordNotification(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("심사 정보를 찾을 수 없습니다"));

        review.recordNotification();
        return toResponse(review);
    }

    /**
     * 심사 이력 목록을 시간순으로 조회한다.
     */
    public List<ReviewHistoryResponse> getHistories(Long reviewId) {
        List<ReviewHistory> histories = reviewHistoryRepository
                .findByReviewIdOrderByChangedAtDesc(reviewId);

        return histories.stream()
                .map(h -> ReviewHistoryResponse.from(h, objectMapper))
                .toList();
    }

    /**
     * 변경 전 스냅샷을 TB_REVIEW_HISTORY에 저장한다.
     */
    private void saveSnapshot(Review review) {
        try {
            String snapshot = objectMapper.writeValueAsString(ReviewSnapshot.from(review));
            ReviewHistory history = ReviewHistory.builder()
                    .reviewId(review.getId())
                    .reviewRound(review.getReviewRound())
                    .snapshot(snapshot)
                    .changedBy(getCurrentUsername())
                    .build();
            reviewHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("심사 이력 스냅샷 저장 실패: reviewId={}", review.getId(), e);
        }
    }

    /**
     * 심사 결과(classification)를 Dead/Wounded 레코드에 자동 반영한다.
     * 단일 트랜잭션 내에서 처리하여 일관성을 보장한다.
     */
    private void applyClassificationToRecord(Review review) {
        if (review.getClassification() == null) {
            return;
        }

        String sn = review.getServiceNumber();

        switch (review.getClassification()) {
            case COMBAT_WOUND -> {
                // Dead 우선 탐색
                var deadOpt = deadRepository.findByServiceNumber(sn);
                if (deadOpt.isPresent()) {
                    Dead dead = deadOpt.get();
                    // deathTypeId는 전공상에 해당하는 코드 (null 허용, 코드 미확정 시)
                    try {
                        dead.updateStatus(DeadStatus.CONFIRMED);
                    } catch (IllegalStateException e) {
                        log.warn("Dead 상태 전이 불가 (전공상 자동반영): serviceNumber={}, {}", sn, e.getMessage());
                    }
                } else {
                    // Wounded fallback
                    var woundedOpt = woundedRepository.findByServiceNumber(sn);
                    if (woundedOpt.isPresent()) {
                        Wounded wounded = woundedOpt.get();
                        wounded.updateWoundType(WoundType.COMBAT_WOUND);
                        try {
                            wounded.updateStatus(WoundedStatus.CONFIRMED);
                        } catch (IllegalStateException e) {
                            log.warn("Wounded 상태 전이 불가 (전공상 자동반영): serviceNumber={}, {}", sn, e.getMessage());
                        }
                    } else {
                        log.info("COMBAT_WOUND 자동반영 대상 없음: serviceNumber={}", sn);
                    }
                }
            }
            case DUTY_WOUND -> {
                var woundedOpt = woundedRepository.findByServiceNumber(sn);
                if (woundedOpt.isPresent()) {
                    Wounded wounded = woundedOpt.get();
                    wounded.updateWoundType(WoundType.DUTY_WOUND);
                    try {
                        wounded.updateStatus(WoundedStatus.CONFIRMED);
                    } catch (IllegalStateException e) {
                        log.warn("Wounded 상태 전이 불가 (공상 자동반영): serviceNumber={}, {}", sn, e.getMessage());
                    }
                } else {
                    log.info("DUTY_WOUND 자동반영 대상 없음: serviceNumber={}", sn);
                }
            }
            case REJECTED -> log.info("심사 기각 처리: reviewId={}", review.getId());
            case DEFERRED -> log.info("심사 보류 처리: reviewId={}", review.getId());
        }
    }

    /**
     * Review 엔티티를 ReviewResponse로 변환한다.
     */
    private ReviewResponse toResponse(Review review) {
        String role = getCurrentUserRole();
        String maskedSsn = RrnMaskingUtil.mask(review.getSsnEncrypted(), role);

        String rankName = review.getRankId() != null
                ? rankCodeRepository.findById(review.getRankId()).map(r -> r.getRankName()).orElse("")
                : "";
        String branchName = review.getBranchId() != null
                ? branchCodeRepository.findById(review.getBranchId()).map(b -> b.getBranchName()).orElse("")
                : "";
        String unitName = review.getUnitId() != null
                ? unitCodeRepository.findById(review.getUnitId()).map(u -> u.getUnitName()).orElse("")
                : "";

        return ReviewResponse.from(review, maskedSsn, rankName, branchName, unitName);
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
