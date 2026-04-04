package com.navy.casualty.admin.dto;

/**
 * 데이터 정합성 확인 응답 DTO.
 * 코드 테이블 건수와 admin 계정 존재 여부를 반환한다.
 */
public record DataCheckResponse(
        long rankCodeCount,
        long branchCodeCount,
        long deathTypeCodeCount,
        long unitCodeCount,
        long veteransOfficeCount,
        boolean adminExists,
        String appVersion
) {}
