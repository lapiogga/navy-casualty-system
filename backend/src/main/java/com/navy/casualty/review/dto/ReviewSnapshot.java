package com.navy.casualty.review.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.navy.casualty.review.entity.Review;

/**
 * 심사 이력 스냅샷 DTO.
 * 주민번호(ssnEncrypted/ssnHash)는 민감정보이므로 절대 포함하지 않는다.
 */
public record ReviewSnapshot(
        Integer reviewRound,
        LocalDate reviewDate,
        String name,
        String serviceNumber,
        LocalDate birthDate,
        LocalDate enlistmentDate,
        Long unitId,
        Long rankId,
        Long branchId,
        String diseaseName,
        String unitReviewResult,
        String classification,
        String status,
        LocalDateTime notificationDate
) {

    /**
     * Review 엔티티로부터 스냅샷을 생성한다.
     */
    public static ReviewSnapshot from(Review review) {
        return new ReviewSnapshot(
                review.getReviewRound(),
                review.getReviewDate(),
                review.getName(),
                review.getServiceNumber(),
                review.getBirthDate(),
                review.getEnlistmentDate(),
                review.getUnitId(),
                review.getRankId(),
                review.getBranchId(),
                review.getDiseaseName(),
                review.getUnitReviewResult(),
                review.getClassification() != null ? review.getClassification().name() : null,
                review.getStatus() != null ? review.getStatus().name() : null,
                review.getNotificationDate()
        );
    }
}
