package com.navy.casualty.wounded.dto;

import java.time.LocalDate;

import com.navy.casualty.wounded.entity.WoundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 상이자 등록 요청 DTO.
 */
public record WoundedCreateRequest(
        @NotBlank(message = "군번은 필수입니다") String serviceNumber,
        @NotBlank(message = "성명은 필수입니다") String name,
        @NotBlank(message = "주민등록번호는 필수입니다") String ssn,
        @NotNull(message = "생년월일은 필수입니다") LocalDate birthDate,
        Long rankId,
        Long branchId,
        Long unitId,
        LocalDate enlistmentDate,
        String phone,
        String address,
        Long veteransOfficeId,
        String diseaseName,
        @NotNull(message = "상이구분은 필수입니다") WoundType woundType
) {
}
