package com.navy.casualty.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.navy.casualty.review.entity.ReviewStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReviewStatus 4단계 상태 전이 규칙 단위 테스트.
 */
class ReviewStatusTest {

    @Test
    @DisplayName("REGISTERED -> UNDER_REVIEW 전이 가능")
    void registered_to_under_review_allowed() {
        assertThat(ReviewStatus.REGISTERED.canTransitionTo(ReviewStatus.UNDER_REVIEW)).isTrue();
    }

    @Test
    @DisplayName("UNDER_REVIEW -> CONFIRMED 전이 가능")
    void under_review_to_confirmed_allowed() {
        assertThat(ReviewStatus.UNDER_REVIEW.canTransitionTo(ReviewStatus.CONFIRMED)).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED -> NOTIFIED 전이 가능")
    void confirmed_to_notified_allowed() {
        assertThat(ReviewStatus.CONFIRMED.canTransitionTo(ReviewStatus.NOTIFIED)).isTrue();
    }

    @Test
    @DisplayName("REGISTERED -> CONFIRMED 건너뛰기 불가")
    void registered_to_confirmed_not_allowed() {
        assertThat(ReviewStatus.REGISTERED.canTransitionTo(ReviewStatus.CONFIRMED)).isFalse();
    }

    @Test
    @DisplayName("NOTIFIED -> 어떤 상태로든 전이 불가")
    void notified_is_terminal() {
        for (ReviewStatus status : ReviewStatus.values()) {
            assertThat(ReviewStatus.NOTIFIED.canTransitionTo(status)).isFalse();
        }
    }
}
