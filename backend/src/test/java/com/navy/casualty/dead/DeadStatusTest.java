package com.navy.casualty.dead;

import static org.assertj.core.api.Assertions.assertThat;

import com.navy.casualty.dead.entity.DeadStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * DeadStatus 상태 전이 규칙 단위 테스트.
 */
class DeadStatusTest {

    @Test
    @DisplayName("REGISTERED -> CONFIRMED 전이 가능")
    void registered_to_confirmed_allowed() {
        assertThat(DeadStatus.REGISTERED.canTransitionTo(DeadStatus.CONFIRMED)).isTrue();
    }

    @Test
    @DisplayName("CONFIRMED -> NOTIFIED 전이 가능")
    void confirmed_to_notified_allowed() {
        assertThat(DeadStatus.CONFIRMED.canTransitionTo(DeadStatus.NOTIFIED)).isTrue();
    }

    @Test
    @DisplayName("REGISTERED -> NOTIFIED 직접 전이 불가")
    void registered_to_notified_not_allowed() {
        assertThat(DeadStatus.REGISTERED.canTransitionTo(DeadStatus.NOTIFIED)).isFalse();
    }

    @Test
    @DisplayName("NOTIFIED -> REGISTERED 전이 불가")
    void notified_to_registered_not_allowed() {
        assertThat(DeadStatus.NOTIFIED.canTransitionTo(DeadStatus.REGISTERED)).isFalse();
    }

    @Test
    @DisplayName("NOTIFIED -> CONFIRMED 전이 불가")
    void notified_to_confirmed_not_allowed() {
        assertThat(DeadStatus.NOTIFIED.canTransitionTo(DeadStatus.CONFIRMED)).isFalse();
    }

    @Test
    @DisplayName("CONFIRMED -> REGISTERED 역방향 전이 불가")
    void confirmed_to_registered_not_allowed() {
        assertThat(DeadStatus.CONFIRMED.canTransitionTo(DeadStatus.REGISTERED)).isFalse();
    }
}
