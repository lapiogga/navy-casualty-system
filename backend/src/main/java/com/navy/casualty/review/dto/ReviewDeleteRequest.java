package com.navy.casualty.review.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 전공사상심사 논리 삭제 요청 DTO. 삭제 사유 필수.
 */
public record ReviewDeleteRequest(
        @NotBlank(message = "삭제 사유는 필수입니다") String reason
) {
}
