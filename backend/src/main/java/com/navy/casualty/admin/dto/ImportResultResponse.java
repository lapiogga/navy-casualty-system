package com.navy.casualty.admin.dto;

import java.util.List;

/**
 * Excel 임포트 결과 응답 DTO.
 */
public record ImportResultResponse(
        int totalRows,
        int successRows,
        int errorRows,
        List<ImportError> errors
) {
    /**
     * 임포트 오류 상세 정보.
     */
    public record ImportError(int rowNumber, String column, String reason) {}
}
