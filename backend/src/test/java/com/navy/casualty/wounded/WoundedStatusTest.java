package com.navy.casualty.wounded;

import static org.assertj.core.api.Assertions.assertThat;

import com.navy.casualty.wounded.entity.WoundedStatus;
import com.navy.casualty.wounded.entity.WoundType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * WoundedStatus 상태 전이 + WoundType 라벨 단위 테스트.
 */
class WoundedStatusTest {

    @Test
    @DisplayName("REGISTERED -> UNDER_REVIEW 전이 가능")
    void registered_to_underReview_allowed() {
        assertThat(WoundedStatus.REGISTERED.canTransitionTo(WoundedStatus.UNDER_REVIEW)).isTrue();
    }

    @Test
    @DisplayName("UNDER_REVIEW -> CONFIRMED 전이 가능")
    void underReview_to_confirmed_allowed() {
        assertThat(WoundedStatus.UNDER_REVIEW.canTransitionTo(WoundedStatus.CONFIRMED)).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED -> NOTIFIED 전이 가능")
    void confirmed_to_notified_allowed() {
        assertThat(WoundedStatus.CONFIRMED.canTransitionTo(WoundedStatus.NOTIFIED)).isTrue();
    }

    @Test
    @DisplayName("REGISTERED -> CONFIRMED 단계 건너뛰기 불가")
    void registered_to_confirmed_not_allowed() {
        assertThat(WoundedStatus.REGISTERED.canTransitionTo(WoundedStatus.CONFIRMED)).isFalse();
    }

    @Test
    @DisplayName("REGISTERED -> NOTIFIED 직접 전이 불가")
    void registered_to_notified_not_allowed() {
        assertThat(WoundedStatus.REGISTERED.canTransitionTo(WoundedStatus.NOTIFIED)).isFalse();
    }

    @Test
    @DisplayName("NOTIFIED -> 모든 상태 전이 불가")
    void notified_to_any_not_allowed() {
        for (WoundedStatus status : WoundedStatus.values()) {
            assertThat(WoundedStatus.NOTIFIED.canTransitionTo(status)).isFalse();
        }
    }

    @Test
    @DisplayName("CONFIRMED -> REGISTERED 역방향 전이 불가")
    void confirmed_to_registered_not_allowed() {
        assertThat(WoundedStatus.CONFIRMED.canTransitionTo(WoundedStatus.REGISTERED)).isFalse();
    }

    @Test
    @DisplayName("WoundType 라벨 확인")
    void woundType_labels() {
        assertThat(WoundType.COMBAT_WOUND.getLabel()).isEqualTo("전공상");
        assertThat(WoundType.DUTY_WOUND.getLabel()).isEqualTo("공상");
        assertThat(WoundType.GENERAL_WOUND.getLabel()).isEqualTo("일반상이");
    }
}
