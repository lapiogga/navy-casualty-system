package com.navy.casualty.statistics.dto;

/**
 * 부대별 사망자 집계 응답 DTO.
 */
public record UnitStatResponse(String unitName, Long count) {
}
