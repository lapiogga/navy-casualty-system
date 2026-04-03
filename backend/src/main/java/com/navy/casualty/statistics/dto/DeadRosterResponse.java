package com.navy.casualty.statistics.dto;

import java.time.LocalDate;

/**
 * 사망자 명부 응답 DTO.
 * 코드 테이블 JOIN 결과 포함, 주민번호는 역할별 마스킹 적용.
 */
public record DeadRosterResponse(
        Long id,
        String branchName,
        String serviceNumber,
        String name,
        String ssnMasked,
        String rankName,
        String unitName,
        LocalDate deathDate,
        String deathTypeName,
        String status
) {
}
