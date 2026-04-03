package com.navy.casualty.dead.dto;

import java.time.LocalDate;

import com.navy.casualty.dead.entity.DeadStatus;

/**
 * 사망자 검색 요청 DTO. 모든 필드 nullable (동적 조건).
 */
public record DeadSearchRequest(
        Long branchId,
        String serviceNumber,
        String name,
        LocalDate birthDate,
        Long rankId,
        Long unitId,
        Long deathTypeId,
        DeadStatus status
) {
}
