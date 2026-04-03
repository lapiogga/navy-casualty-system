package com.navy.casualty.wounded.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 상이자 논리 삭제 요청 DTO. 삭제 사유 필수.
 */
public record WoundedDeleteRequest(
        @NotBlank(message = "삭제 사유는 필수입니다") String reason
) {
}
