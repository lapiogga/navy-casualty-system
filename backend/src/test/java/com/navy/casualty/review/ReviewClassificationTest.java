package com.navy.casualty.review;

import static org.assertj.core.api.Assertions.assertThat;

import com.navy.casualty.review.entity.ReviewClassification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ReviewClassification 4-value enum 라벨 단위 테스트.
 */
class ReviewClassificationTest {

    @Test
    @DisplayName("COMBAT_WOUND 라벨은 전공상")
    void combat_wound_label() {
        assertThat(ReviewClassification.COMBAT_WOUND.getLabel()).isEqualTo("전공상");
    }

    @Test
    @DisplayName("DUTY_WOUND 라벨은 공상")
    void duty_wound_label() {
        assertThat(ReviewClassification.DUTY_WOUND.getLabel()).isEqualTo("공상");
    }

    @Test
    @DisplayName("REJECTED 라벨은 기각")
    void rejected_label() {
        assertThat(ReviewClassification.REJECTED.getLabel()).isEqualTo("기각");
    }

    @Test
    @DisplayName("DEFERRED 라벨은 보류")
    void deferred_label() {
        assertThat(ReviewClassification.DEFERRED.getLabel()).isEqualTo("보류");
    }
}
