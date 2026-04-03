package com.navy.casualty.review.dto;

import java.time.LocalDate;

import com.navy.casualty.review.entity.ReviewClassification;
import jakarta.validation.constraints.NotBlank;

/**
 * 전공사상심사 수정 요청 DTO. 군번은 수정 불가.
 */
public record ReviewUpdateRequest(
        Integer reviewRound,
        LocalDate reviewDate,
        @NotBlank(message = "성명은 필수입니다") String name,
        String ssn,
        LocalDate birthDate,
        Long rankId,
        Long branchId,
        Long unitId,
        LocalDate enlistmentDate,
        String diseaseName,
        String unitReviewResult,
        ReviewClassification classification
) {
}
