package com.navy.casualty.dead.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 사망자 등록 요청 DTO.
 */
public record DeadCreateRequest(
        @NotBlank(message = "군번은 필수입니다") String serviceNumber,
        @NotBlank(message = "성명은 필수입니다") String name,
        @NotBlank(message = "주민등록번호는 필수입니다") String ssn,
        @NotNull(message = "생년월일은 필수입니다") LocalDate birthDate,
        Long rankId,
        Long branchId,
        Long unitId,
        LocalDate enlistmentDate,
        String phone,
        Long deathTypeId,
        Long deathCodeId,
        String address,
        @NotNull(message = "사망일자는 필수입니다") LocalDate deathDate
) {
}
