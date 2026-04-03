package com.navy.casualty.wounded.dto;

import java.time.LocalDate;

import com.navy.casualty.wounded.entity.WoundType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 상이자 수정 요청 DTO. 군번은 수정 불가.
 */
public record WoundedUpdateRequest(
        @NotBlank(message = "성명은 필수입니다") String name,
        String ssn,
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
