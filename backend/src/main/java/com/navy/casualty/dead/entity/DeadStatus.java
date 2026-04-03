package com.navy.casualty.dead.entity;

/**
 * 사망자 상태 enum.
 * 상태 전이: REGISTERED -> CONFIRMED -> NOTIFIED (단방향).
 */
public enum DeadStatus {

    REGISTERED,
    CONFIRMED,
    NOTIFIED;

    /**
     * 다음 상태로 전이 가능 여부를 판단한다.
     */
    public boolean canTransitionTo(DeadStatus next) {
        return switch (this) {
            case REGISTERED -> next == CONFIRMED;
            case CONFIRMED -> next == NOTIFIED;
            case NOTIFIED -> false;
        };
    }
}
