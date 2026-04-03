package com.navy.casualty.wounded.entity;

/**
 * 상이자 상태 enum.
 * 상태 전이: REGISTERED -> UNDER_REVIEW -> CONFIRMED -> NOTIFIED (단방향 4단계).
 */
public enum WoundedStatus {

    REGISTERED,
    UNDER_REVIEW,
    CONFIRMED,
    NOTIFIED;

    /**
     * 다음 상태로 전이 가능 여부를 판단한다.
     */
    public boolean canTransitionTo(WoundedStatus next) {
        return switch (this) {
            case REGISTERED -> next == UNDER_REVIEW;
            case UNDER_REVIEW -> next == CONFIRMED;
            case CONFIRMED -> next == NOTIFIED;
            case NOTIFIED -> false;
        };
    }
}
