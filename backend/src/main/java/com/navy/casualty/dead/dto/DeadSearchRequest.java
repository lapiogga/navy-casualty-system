package com.navy.casualty.dead.dto;

import com.navy.casualty.dead.entity.DeadStatus;

import java.time.LocalDate;

/**
 * 사망자 검색 조건 DTO.
 * 모든 필드 nullable (미입력 시 전체 조회).
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
