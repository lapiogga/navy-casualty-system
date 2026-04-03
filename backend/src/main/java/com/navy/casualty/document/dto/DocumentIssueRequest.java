package com.navy.casualty.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 문서 발급 요청 DTO. 발급 목적 필수 입력 (DOCU-08).
 */
public record DocumentIssueRequest(

        @NotBlank(message = "발급 목적은 필수입니다")
        @Size(max = 500, message = "발급 목적은 500자 이내로 입력하세요")
        String purpose
) {
}
