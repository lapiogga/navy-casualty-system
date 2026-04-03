package com.navy.casualty.dead.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.navy.casualty.dead.entity.Dead;

/**
 * 사망자 응답 DTO.
 */
public record DeadResponse(
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
        String deathTypeName,
        String deathCodeSymbol,
        String address,
        LocalDate deathDate,
        String status,
        LocalDateTime createdAt
) {

    /**
     * Dead 엔티티로부터 응답 DTO를 생성한다.
     */
    public static DeadResponse from(Dead dead, String maskedSsn,
                                     String rankName, String branchName, String unitName,
                                     String deathTypeName, String deathCodeSymbol) {
        return new DeadResponse(
                dead.getId(),
                dead.getServiceNumber(),
                dead.getName(),
                maskedSsn,
                dead.getBirthDate(),
                rankName,
                branchName,
                unitName,
                dead.getEnlistmentDate(),
                dead.getPhone(),
                deathTypeName,
                deathCodeSymbol,
                dead.getAddress(),
                dead.getDeathDate(),
                dead.getStatus().name(),
                dead.getCreatedAt()
        );
    }
}
