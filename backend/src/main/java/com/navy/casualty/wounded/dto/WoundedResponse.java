package com.navy.casualty.wounded.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.navy.casualty.wounded.entity.Wounded;

/**
 * 상이자 응답 DTO.
 */
public record WoundedResponse(
        Long id,
        String serviceNumber,
        String name,
        String ssnMasked,
        LocalDate birthDate,
        String rankName,
        String branchName,
        String unitName,
        LocalDate enlistmentDate,
        String phone,
        String address,
        String veteransOfficeName,
        String diseaseName,
        String woundTypeName,
        String status,
        LocalDateTime createdAt
) {

    /**
     * Wounded 엔티티로부터 응답 DTO를 생성한다.
     */
    public static WoundedResponse from(Wounded w, String maskedSsn,
                                        String rankName, String branchName, String unitName,
                                        String veteransOfficeName) {
        return new WoundedResponse(
                w.getId(),
                w.getServiceNumber(),
                w.getName(),
                maskedSsn,
                w.getBirthDate(),
                rankName,
                branchName,
                unitName,
                w.getEnlistmentDate(),
                w.getPhone(),
                w.getAddress(),
                veteransOfficeName,
                w.getDiseaseName(),
                w.getWoundType() != null ? w.getWoundType().getLabel() : "",
                w.getStatus().name(),
                w.getCreatedAt()
        );
    }
}
