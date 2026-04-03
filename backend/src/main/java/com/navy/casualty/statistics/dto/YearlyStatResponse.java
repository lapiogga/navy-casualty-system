package com.navy.casualty.statistics.dto;

/**
 * 연도별 사망자 집계 응답 DTO.
 */
public record YearlyStatResponse(Integer year, Long count) {
}
