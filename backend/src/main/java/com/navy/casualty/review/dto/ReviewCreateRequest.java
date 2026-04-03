package com.navy.casualty.review.dto;

import java.time.LocalDate;

import com.navy.casualty.review.entity.ReviewClassification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 전공사상심사 등록 요청 DTO.
 */
public record ReviewCreateRequest(
        @NotNull(message = "심사차수는 필수입니다") Integer reviewRound,
        LocalDate reviewDate,
        @NotBlank(message = "성명은 필수입니다") String name,
        @NotBlank(message = "군번은 필수입니다") String serviceNumber,
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
