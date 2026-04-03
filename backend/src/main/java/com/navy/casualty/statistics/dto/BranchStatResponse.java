package com.navy.casualty.statistics.dto;

/**
 * 신분별 사망자 집계 응답 DTO.
 */
public record BranchStatResponse(String branchName, Long count) {
}
