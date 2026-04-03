package com.navy.casualty.dead.entity;

/**
 * 사망자 상태 열거형.
 * REGISTERED -> CONFIRMED -> NOTIFIED 순서로만 전이 가능.
 */
public enum DeadStatus {
    REGISTERED,
    CONFIRMED,
    NOTIFIED;

    /**
     * 현재 상태에서 다음 상태로 전이 가능한지 검증한다.
     */
    public boolean canTransitionTo(DeadStatus next) {
        return switch (this) {
            case REGISTERED -> next == CONFIRMED;
            case CONFIRMED -> next == NOTIFIED;
            case NOTIFIED -> false;
        };
    }
}
