package com.navy.casualty.wounded.dto;

import java.time.LocalDate;

import com.navy.casualty.wounded.entity.WoundType;
import com.navy.casualty.wounded.entity.WoundedStatus;

/**
 * 상이자 검색 요청 DTO. 모든 필드 nullable (동적 조건).
 */
public record WoundedSearchRequest(
        Long branchId,
        String serviceNumber,
        String name,
        LocalDate birthDate,
        Long rankId,
        Long unitId,
        WoundType woundType,
        WoundedStatus status
) {
}
