package com.navy.casualty.review.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.navy.casualty.review.entity.Review;

/**
 * 전공사상심사 응답 DTO.
 */
public record ReviewResponse(
        Long id,
        Integer reviewRound,
        LocalDate reviewDate,
        String name,
        String serviceNumber,
        String ssnMasked,
        LocalDate birthDate,
        String rankName,
        String branchName,
        String unitName,
        LocalDate enlistmentDate,
        String diseaseName,
        String unitReviewResult,
        String classificationName,
        String status,
        LocalDateTime notificationDate,
        LocalDateTime createdAt
) {

    /**
     * Review 엔티티로부터 응답 DTO를 생성한다.
     */
    public static ReviewResponse from(Review r, String maskedSsn,
                                       String rankName, String branchName, String unitName) {
        return new ReviewResponse(
                r.getId(),
                r.getReviewRound(),
                r.getReviewDate(),
                r.getName(),
                r.getServiceNumber(),
                maskedSsn,
                r.getBirthDate(),
                rankName,
                branchName,
                unitName,
                r.getEnlistmentDate(),
                r.getDiseaseName(),
                r.getUnitReviewResult(),
                r.getClassification() != null ? r.getClassification().getLabel() : "",
                r.getStatus().name(),
                r.getNotificationDate(),
                r.getCreatedAt()
        );
    }
}
