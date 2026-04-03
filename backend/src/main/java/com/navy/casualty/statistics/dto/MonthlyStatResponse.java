package com.navy.casualty.statistics.dto;

/**
 * 월별 사망자 집계 응답 DTO.
 */
public record MonthlyStatResponse(Integer year, Integer month, Long count) {
}
