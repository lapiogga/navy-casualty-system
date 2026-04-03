package com.navy.casualty.review.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navy.casualty.review.entity.ReviewHistory;
import lombok.extern.slf4j.Slf4j;

/**
 * 전공사상심사 이력 응답 DTO.
 */
public record ReviewHistoryResponse(
        Long id,
        Long reviewId,
        Integer reviewRound,
        ReviewSnapshot snapshot,
        LocalDateTime changedAt,
        String changedBy
) {

    /**
     * ReviewHistory 엔티티로부터 응답 DTO를 생성한다.
     */
    public static ReviewHistoryResponse from(ReviewHistory h, ObjectMapper mapper) {
        ReviewSnapshot snap = null;
        try {
            snap = mapper.readValue(h.getSnapshot(), ReviewSnapshot.class);
        } catch (Exception e) {
            // 스냅샷 파싱 실패 시 null로 반환
        }
        return new ReviewHistoryResponse(
                h.getId(),
                h.getReviewId(),
                h.getReviewRound(),
                snap,
                h.getChangedAt(),
                h.getChangedBy()
        );
    }
}
