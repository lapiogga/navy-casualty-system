package com.navy.casualty.review.dto;

import java.time.LocalDate;

import com.navy.casualty.review.entity.ReviewClassification;
import com.navy.casualty.review.entity.ReviewStatus;

/**
 * 전공사상심사 검색 요청 DTO. 모든 필드 nullable (동적 조건).
 */
public record ReviewSearchRequest(
        Long branchId,
        String serviceNumber,
        String name,
        LocalDate birthDate,
        Long rankId,
        Long unitId,
        ReviewClassification classification,
        ReviewStatus status
) {
}
