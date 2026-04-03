package com.navy.casualty.wounded.entity;

import lombok.Getter;

/**
 * 상이구분 enum.
 * 전공상(COMBAT_WOUND), 공상(DUTY_WOUND), 일반상이(GENERAL_WOUND).
 */
@Getter
public enum WoundType {

    COMBAT_WOUND("전공상"),
    DUTY_WOUND("공상"),
    GENERAL_WOUND("일반상이");

    private final String label;

    WoundType(String label) {
        this.label = label;
    }
}
